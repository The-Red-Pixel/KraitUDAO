/*
 * StandardDataObjectInterpreter.java
 *
 * Copyright (C) 2018 The Red Pixel <theredpixelteam.com>
 * Copyright (C) 2018 KuCrO3 Studio <kucro3.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.theredpixelteam.kraitudao.interpreter;

import com.theredpixelteam.kraitudao.PlaceHolder;
import com.theredpixelteam.kraitudao.annotations.*;
import com.theredpixelteam.kraitudao.annotations.expandable.*;
import com.theredpixelteam.kraitudao.annotations.inheritance.*;
import com.theredpixelteam.kraitudao.annotations.metadata.ExpandedName;
import com.theredpixelteam.kraitudao.annotations.metadata.Metadata;
import com.theredpixelteam.kraitudao.annotations.metadata.MetadataCollection;
import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.redtea.predication.MultiCondition;
import com.theredpixelteam.redtea.predication.MultiPredicate;
import com.theredpixelteam.redtea.predication.NamedPredicate;
import com.theredpixelteam.redtea.util.Reference;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class StandardDataObjectInterpreter implements DataObjectInterpreter {
    public StandardDataObjectInterpreter()
    {
        this(Collections.emptyMap());
    }

    public StandardDataObjectInterpreter(Map<Class<?>, ExpandRule> bulitInRules)
    {
        this.bulitInRules = Collections.unmodifiableMap(bulitInRules);
    }

    @Override
    public DataObject get(Class<?> type) throws DataObjectInterpretationException
    {
        Objects.requireNonNull(type, "type");

        int i = (type.getAnnotation(Unique.class) == null ? 0b00 : 0b01)
                | (type.getAnnotation(Multiple.class) == null ? 0b00 : 0b10);

        switch(i)
        {
            case 0b00: // not annotated
                throw new DataObjectInterpretationException("Metadata annotation not found in type: " + type.getCanonicalName());

            case 0b01: // only annotated by @Unique
                return getUnique0(type);

            case 0b10: // only annotated by @Multiple
                return getMultiple0(type);

            case 0b11: // both annotated
                throw new DataObjectInterpretationException("Duplicated metadata annotation in type: " + type.getCanonicalName());
        }

        throw new IllegalStateException("Should not reach here");
    }

    @Override
    public MultipleDataObject getMultiple(Class<?> type) throws DataObjectInterpretationException
    {
        Objects.requireNonNull(type, "type");

        if(type.getAnnotation(Multiple.class) == null)
            throw new DataObjectInterpretationException("Type: " + type.getCanonicalName() + " is not annotated by @Multiple");

        return getMultiple0(type);
    }

    @Override
    public UniqueDataObject getUnique(Class<?> type) throws DataObjectInterpretationException
    {
        Objects.requireNonNull(type, "type");

        if(type.getAnnotation(Unique.class) == null)
            throw new DataObjectInterpretationException("Type: " + type.getCanonicalName() + " is not annotated by @Unique");

        return getUnique0(type);
    }

    @Override
    public DataObject expand(DataObject dataObject) throws DataObjectInterpretationException
    {
        DataObjectContainer dataObjectContainer;

        if(dataObject instanceof MultipleDataObject)
        {
            MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
            dataObjectContainer = new MultipleDataObjectContainer(dataObject.getType());

            dataObjectContainer.putKey(KeyType.PRIMARY, multipleDataObject.getPrimaryKey());

            for(ValueObject secondaryKey : multipleDataObject.getSecondaryKeys().values())
                if(!secondaryKey.getExpandRule().isPresent())
                    dataObjectContainer.putKey(KeyType.SECONDARY, secondaryKey);
                else
                {
                    ExpandRule expandRule = secondaryKey.getExpandRule().get();

                    for(ExpandRule.Entry entry : expandRule.getEntries())
                    {
                        Class<?> type = entry.getExpandedType();
                        String name = entry.name();

                        ValueObjectContainer valueObjectContainer = new ValueObjectContainer(dataObject.getType(), type, REFLECTION_COMPATIBLE_TYPES.get(type));
                        valueObjectContainer.name = name;
                        valueObjectContainer.secondaryKey = true;
                        valueObjectContainer.owner = dataObject;
                        valueObjectContainer.metadata = entry.getMetadataMap();

                        expand(dataObject, secondaryKey, valueObjectContainer, entry);

                        valueObjectContainer.seal();

                        dataObjectContainer.putKey(KeyType.SECONDARY, valueObjectContainer);
                    }
                }
        }
        else if(dataObject instanceof UniqueDataObject)
        {
            UniqueDataObject uniqueDataObject = (UniqueDataObject) dataObject;
            dataObjectContainer = new UniqueDataObjectContainer(dataObject.getType());

            dataObjectContainer.putKey(KeyType.UNIQUE, uniqueDataObject.getKey());
        }
        else
            throw new DataObjectMalformationException("Illegal type of data object");

        for(ValueObject value : dataObject.getValues().values())
            if(!value.getExpandRule().isPresent())
                dataObjectContainer.putValue(value);
            else
            {
                ExpandRule expandRule = value.getExpandRule().get();

                for(ExpandRule.Entry entry : expandRule.getEntries())
                {
                    Class<?> type = entry.getExpandedType();
                    String name = entry.name();

                    ValueObjectContainer valueObjectContainer = new ValueObjectContainer(dataObject.getType(), type, REFLECTION_COMPATIBLE_TYPES.get(type));
                    valueObjectContainer.name = name;
                    valueObjectContainer.owner = dataObject;
                    valueObjectContainer.metadata = entry.getMetadataMap();

                    expand(dataObject, value, valueObjectContainer, entry);

                    valueObjectContainer.seal();

                    dataObjectContainer.putValue(valueObjectContainer);
                }
            }

        return dataObjectContainer;
    }

    private static void expand(DataObject dataObject,
                                         ValueObject origin,
                                         ValueObjectContainer valueObjectContainer,
                                         ExpandRule.Entry entry)
            throws DataObjectInterpretationException
    {
        Method m0, m1;
        Class<?> type = entry.getExpandedType();

        ExpandRule.At getter = entry.getterInfo();
        ExpandRule.At setter = entry.setterInfo();

        switch(getter.source())
        {
            case THIS:
                try {
                    checkReturnType(m0 = dataObject.getType().getMethod(getter.name(), type), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.getter = (object) -> {
                    try {
                        return m0.invoke(object, origin.get(object));
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;

            case FIELD:
                try {
                    checkReturnType(m0 = origin.getType().getMethod(getter.name()), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.getter = (object) -> {
                    try {
                        return m0.invoke(origin.get(object));
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;
        }

        switch(setter.source())
        {
            case THIS:
                try {
                    m1 = dataObject.getType().getMethod(setter.name(), origin.getType(), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.setter = (object, value) -> {
                    try {
                        m1.invoke(object, origin.get(object), value);
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;

            case FIELD:
                try {
                    m1 = origin.getType().getMethod(setter.name(), type);
                } catch (NoSuchMethodException e) {
                    throw new DataObjectMalformationException(String.format("Expanding secondary key (Name: %s, Entry: %s)",
                            valueObjectContainer.getName(),
                            entry.name()), e);
                }

                valueObjectContainer.setter = (object, value) -> {
                    try {
                        m1.invoke(origin.get(object), value);
                    } catch (Exception e) {
                        throw new DataObjectError("Reflection error", e);
                    }
                };
                break;
        }
    }

    private static void checkReturnType(Method method, Class<?> required) throws NoSuchMethodException
    {
        if(!method.getReturnType().equals(required))
            throw new NoSuchMethodException(String.format("Incompatible return type (Found: %s, Required: %s)",
                    method.getReturnType().getCanonicalName(),
                    required.getCanonicalName()));
    }

    private MultipleDataObject getMultiple0(Class<?> type) throws DataObjectInterpretationException
    {
        MultipleDataObjectContainer container = new MultipleDataObjectContainer(type);

        parse(type, container, type.getAnnotation(Inheritance.class) != null, true);
        container.seal();

        return container;
    }

    private UniqueDataObject getUnique0(Class<?> type) throws DataObjectInterpretationException
    {
        UniqueDataObjectContainer container = new UniqueDataObjectContainer(type);

        parse(type, container, type.getAnnotation(Inheritance.class) != null, true);
        container.seal();

        return container;
    }

    private void parse(Class<?> type, DataObjectContainer container, boolean inherited, boolean top) throws DataObjectInterpretationException
    {
        GlobalExpandRules globalRules = new GlobalExpandRules();

        try {
            if (inherited)
                parse(type.getSuperclass(), container, type.getSuperclass().getAnnotation(Inheritance.class) != null, false);

            parseClass(type, container, globalRules, inherited, top);
            parseFields(type, container, globalRules, inherited, top);
            parseMethods(type, container, inherited, top);

            ValueObjectContainer valueObjectContainer;
            for(ValueObject valueObject : container.getValues().values())
                if(!(valueObjectContainer = (ValueObjectContainer) valueObject).sealed)
                    valueObjectContainer.seal();
        } catch (DataObjectInterpretationException e) {
            throw e;
        } catch (Exception e) {
            throw new DataObjectInterpretationException(e);
        }
    }

    private void parseClass(Class<?> type, DataObjectContainer container, GlobalExpandRules globalRules, boolean inherited, boolean top)
            throws DataObjectInterpretationException
    {
        for(BuiltinExpandRule builtinRule : type.getAnnotationsByType(BuiltinExpandRule.class))
        {
            ExpandRule rule;
            Class<?> expanding = builtinRule.value();
            if((rule = this.bulitInRules.get(expanding)) == null)
                throw new DataObjectMalformationException("Unsupported built-in expand rule (Type: " + expanding.getCanonicalName() + ")");

            if(globalRules.putIfAbsent(expanding, rule) != null)
                throw new DataObjectMalformationException("Duplicated expand rule declaration (Type: " + expanding.getCanonicalName() + ")");
        }

        for(CustomExpandRule customRule : type.getAnnotationsByType(CustomExpandRule.class))
        {
            Class<?> expanding = customRule.type();

            if(globalRules.containsKey(expanding))
                throw new DataObjectMalformationException("Duplicated expand rule declaration (Type: " + expanding.getCanonicalName() + ")");

            ExpandRuleContainer expandRuleContainer = new ExpandRuleContainer(customRule.type());

            if(customRule.entries().length == 0)
                throw new DataObjectMalformationException("Global expand rule of type: \"" + expanding.getCanonicalName() + "\" has no entry");

            parseExpandRuleEntries(expandRuleContainer, customRule.entries());

            globalRules.put(expanding, expandRuleContainer);
        }

        for(InheritValue inheritValue : type.getAnnotationsByType(InheritValue.class))
        {
            InheritanceInfo info = new InheritanceInfo(inheritValue);

            try {
                ValueObjectContainer valueObject = parseValueObject(type, container, info);

                valueObject.seal();

                container.putValue(valueObject);
            } catch (Exception e) {
                throw new DataObjectMalformationException(info.toString(), e);
            }
        }

        for(InheritKey inheritKey : type.getAnnotationsByType(InheritKey.class))
        {
            InheritanceInfo info = new InheritanceInfo(inheritKey);

            try {
                ValueObjectContainer valueObject = parseValueObject(type, container, info);

                valueObject.primaryKey = true;
                valueObject.seal();

                container.putKey(KeyType.UNIQUE, valueObject);
            } catch (Exception e) {
                throw new DataObjectMalformationException(info.toString(), e);
            }
        }

        for(InheritPrimaryKey inheritPrimaryKey : type.getAnnotationsByType(InheritPrimaryKey.class))
        {
            InheritanceInfo info = new InheritanceInfo(inheritPrimaryKey);

            try {
                ValueObjectContainer valueObject = parseValueObject(type, container, info);

                valueObject.primaryKey = true;
                valueObject.seal();

                container.putKey(KeyType.PRIMARY, valueObject);
            } catch (Exception e) {
                throw new DataObjectMalformationException(info.toString(), e);
            }
        }

        for(InheritSecondaryKey inheritSecondaryKey : type.getAnnotationsByType(InheritSecondaryKey.class))
        {
            InheritanceInfo info = new InheritanceInfo(inheritSecondaryKey);

            try {
                ValueObjectContainer valueObject = parseValueObject(type, container, info);

                valueObject.secondaryKey = true;
                valueObject.seal();

                container.putKey(KeyType.SECONDARY, valueObject);
            } catch (Exception e) {
                throw new DataObjectMalformationException(info.toString(), e);
            }
        }
    }

    private static ValueObjectContainer parseValueObject(Class<?> type, DataObjectContainer container, InheritanceInfo info)
            throws DataObjectInterpretationException
    {
        Field f = searchAndCheck(type, info);

        ValueObjectContainer voc = new ValueObjectContainer(type, f.getType(), REFLECTION_COMPATIBLE_TYPES.get(f.getType()));
        voc.owner = container;

        voc.name = info.name().isEmpty() ? f.getName() : info.name();

        defaultSetterAndGetter(voc, f);
        parseRules(type, f.getType(), voc, info);

        return voc;
    }

    private static void parseRules(Class<?> type, Class<?> fieldType, ValueObjectContainer valueObject, InheritanceInfo info)
            throws DataObjectInterpretationException
    {
        Entry[] entires = info.expanding().entries();

        if(entires.length == 0)
            return;

        ExpandRuleContainer expandRule = new ExpandRuleContainer(fieldType);

        parseExpandRuleEntries(expandRule, entires);

        valueObject.expandRule = expandRule;
    }

    private static void defaultSetterAndGetter(ValueObjectContainer valueObject, Field field)
    {
        // getter
        valueObject.getter = (obj) -> {
            try {
                return field.get(obj);
            } catch (Exception e) {
                throw new DataObjectError("Reflection error", e);
            }
        };

        // setter
        valueObject.setter = (obj, val) -> {
            try {
                field.set(obj, val);
            } catch (Exception e) {
                throw new DataObjectError("Reflection error", e);
            }
        };
    }

    private static Field searchAndCheck(Class<?> type, InheritanceInfo info)
            throws DataObjectInterpretationException
    {
        Field f = search(type, info);

        if(!info.type().equals(PlaceHolder.class) && !info.type().equals(f.getType()))
            throw new DataObjectMalformationException(String.format(
                    "Incompatible field type: %s (Required: %s)",
                    f.getType().getCanonicalName(),
                    info.type().getCanonicalName())
            );

        return f;
    }

    private static Field search(Class<?> type, InheritanceInfo info)
            throws DataObjectInterpretationException
    {
        if(info.strict() && (type.getAnnotation(Inheritance.class)) == null)
        {
            if(info.source().equals(PlaceHolder.class) || info.source().equals(type))
                try {
                    return type.getDeclaredField(info.name());
                } catch (NoSuchFieldException e) {
                }

            throw new DataObjectMalformationException(String.format(
                    "Field \"%s\" not found",
                    info.field()
            ));
        }

        Class<?> klass = type.getSuperclass();
        Class<?> c = type;

        if(!info.source().equals(PlaceHolder.class))
        {
            while(!c.equals(Object.class))
            {
                if (c.equals(info.source()))
                    try {
                        return c.getDeclaredField(info.field());
                    } catch (NoSuchFieldException e) {
                        throw new DataObjectMalformationException(String.format(
                                "Field \"%s\" not found in source class: %s",
                                info.field(),
                                c.getCanonicalName()), e
                        );
                    }

                if(info.strict() && c.getAnnotation(Inheritance.class) == null)
                    break;

                c = c.getSuperclass();
            }

            throw new DataObjectMalformationException(String.format(
                    "Source class \"%s\" is not in the inhertiance tree",
                    info.source().getCanonicalName()
            ));
        }
        else
        {
            while(!c.equals(Object.class))
            {
                try {
                    return c.getDeclaredField(info.field());
                } catch (NoSuchFieldException e) {
                }

                if(info.strict() && c.getAnnotation(Inheritance.class) == null)
                    break;

                c = c.getSuperclass();
            }

            throw new DataObjectMalformationException(String.format(
                    "Field \"%s\" not found in inheritance tree",
                    info.field()
            ));
        }
    }

    private static void parseExpandRuleEntries(ExpandRuleContainer expandRuleContainer, Entry[] entries)
            throws DataObjectInterpretationException
    {
        for(Entry entry : entries)
        {
            EntryContainer entryContainer = new EntryContainer(entry.name(), entry.type());

            At getterInfo = entry.getter();
            At setterInfo = entry.setter();

            entryContainer.getterInfo = new EntryContainer.AtInfo(getterInfo.name(), getterInfo.source());
            entryContainer.setterInfo = new EntryContainer.AtInfo(setterInfo.name(), setterInfo.source());

            entryContainer.seal();
            expandRuleContainer.entryList.add(entryContainer);
        }
    }

    private void parseFields(Class<?> type, DataObjectContainer container, GlobalExpandRules golbalRules, boolean inherited, boolean top)
            throws DataObjectInterpretationException
    {
        for(Field field : type.getDeclaredFields()) {
            MultiCondition.CurrentCondition<Class<?>> current;

            current = annotationCondition.apply(
                    field,
                    Key.class,
                    PrimaryKey.class,
                    SecondaryKey.class
            );

            Reference<KeyType> keyType = new Reference<>();
            ValueObjectContainer valueObject = new ValueObjectContainer(container.getType(), field.getType(), REFLECTION_COMPATIBLE_TYPES.get(field.getType()));
            valueObject.owner = container;

            Value valueInfo;

            if (current.getCurrent().trueCount() != 0) {
                Reference<String> name = new Reference<>();

                current.completelyOnlyIf(Key.class)
                        .perform(() -> {
                            valueObject.primaryKey = true;
                            keyType.set(KeyType.UNIQUE);
                            name.set(field.getAnnotation(Key.class).value());
                        })
                        .elseCompletelyOnlyIf(PrimaryKey.class)
                        .perform(() -> {
                            valueObject.primaryKey = true;
                            keyType.set(KeyType.PRIMARY);
                            name.set(field.getAnnotation(PrimaryKey.class).value());
                        })
                        .elseCompletelyOnlyIf(SecondaryKey.class)
                        .perform(() -> {
                            valueObject.secondaryKey = true;
                            keyType.set(KeyType.SECONDARY);
                            name.set(field.getAnnotation(SecondaryKey.class).value());
                        })
                        .orElse(() -> {
                            throw new DataObjectMalformationException("Duplicated value object metadata");
                        });

                valueObject.name = name.get().isEmpty() ? field.getName() : name.get();
            } else if ((valueInfo = field.getAnnotation(Value.class)) != null)
                valueObject.name = valueInfo.value().isEmpty() ? field.getName() : valueInfo.value();
            else
                continue;

            field.setAccessible(true);

            defaultSetterAndGetter(valueObject, field);

            // expand rule
            Expandable expandInfo;
            if ((expandInfo = field.getAnnotation(Expandable.class)) != null)
            {
                Entry[] entries = expandInfo.entries();

                if (entries.length == 0)
                    throw new DataObjectMalformationException("Expand rule of \"" + field.getName() + "\" has no entry");

                ExpandRuleContainer expandRuleContainer = new ExpandRuleContainer(field.getType());

                parseExpandRuleEntries(expandRuleContainer, entries);

                expandRuleContainer.seal();

                valueObject.expandRule = expandRuleContainer;
            }

            // metadata
            ExpandRuleContainer rule;
            if ((rule = (ExpandRuleContainer) valueObject.expandRule) == null)
                for (Annotation annotation : field.getDeclaredAnnotations())
                    if (annotation.annotationType().getAnnotation(MetadataCollection.class) == null)
                        if (annotation.annotationType().getAnnotation(Metadata.class) != null)
                            if (valueObject.metadata.putIfAbsent(annotation.annotationType(), annotation) != null)
                                throw new DataObjectMalformationException(String.format(
                                        "Duplicated metadata (Type: @%s, Value name: %s)",
                                        annotation.annotationType().getCanonicalName(),
                                        valueObject.getName()
                                ));
                            else ;
                        else ;
                    else
                        throw new DataObjectMalformationException(
                                "Metadata collection not supported for non-expandable value (Value Name: " + valueObject.getName() + ")");
            else
            {
                Map<String, EntryContainer> map = new HashMap<>();

                for(ExpandRuleContainer.Entry entry : rule.entries)
                    map.put(entry.name(), (EntryContainer) entry);

                Map<Class<?>, Method> cache = new HashMap<>();
                for(Annotation anno : field.getDeclaredAnnotations())
                {
                    MetadataCollection collectionInfo = null;
                    if(anno.annotationType().getAnnotation(Metadata.class) == null
                            && (collectionInfo = anno.annotationType().getAnnotation(MetadataCollection.class)) == null)
                        continue;

                    List<Annotation> annos;

                    if(collectionInfo != null)
                        try {
                            annos = (List) Arrays.asList((Object[]) collectionInfo.annotationType().getMethod("value").invoke(anno));
                        } catch (Exception e) {
                            throw new DataObjectMalformationException(String.format(
                                    "Failed to unpack the metadata collection of %s (Type: %s)",
                                    collectionInfo.value().getCanonicalName(),
                                    collectionInfo.annotationType().getCanonicalName()
                            ));
                        }
                    else
                        annos = Collections.singletonList(anno);

                    for(Annotation annotation : annos)
                    {
                        Class<?> annotationType = annotation.annotationType();
                        Method m;

                        if ((m = cache.get(annotationType)) == null)
                        {
                            for (Method method : annotationType.getMethods())
                                if (method.getAnnotation(ExpandedName.class) != null)
                                {
                                    if (m != null)
                                        throw new DataObjectMalformationException("Duplicated expanded name metadata for @" + annotationType.getCanonicalName());

                                    m = method;
                                }

                            if (m == null)
                                throw new DataObjectMalformationException(
                                        "@" + annotationType.getCanonicalName() + " is not supported for expandable value (@ExpandedName metadata not found)");

                            cache.put(annotationType, m);
                        }

                        String name;
                        try {
                            name = (String) m.invoke(annotation);
                        } catch (Exception e) {
                            throw new DataObjectMalformationException("Unable to get @ExpandedName metadata", e);
                        }

                        EntryContainer ruleEntry;
                        if ((ruleEntry = map.get(name)) == null)
                            throw new DataObjectMalformationException("Incorrect expanded name");

                        if (ruleEntry.metadata.putIfAbsent(annotationType, annotation) != null)
                            throw new DataObjectMalformationException(String.format(
                                    "Duplicated metadata (Type: %s, Value Name: %s)",
                                    annotationType.getCanonicalName(),
                                    valueObject.getName()
                            ));
                    }
                }
            }

            if(top)
                valueObject.seal();

            if(valueObject.isKey())
                container.putKey(keyType.get(), valueObject);
            else
                container.putValue(valueObject);
        }
    }

    static void checkArgumentCount(Class<?>[] arguments, int required, String location, String name)
            throws DataObjectInterpretationException
    {
        if(arguments.length != required)
            throw new DataObjectMalformationException(
                    String.format("%d argument(s) reqiured, %d found (%s, Name: %s)",
                            required,
                            arguments.length,
                            location,
                            name)
            );
    }

    static void checkArgument(Class<?>[] arguments, int index, Class<?> required, String location, String name)
            throws DataObjectInterpretationException
    {
        if(!arguments[index].equals(required))
            throw new DataObjectMalformationException(
                    String.format("Invalid argument type of %s (Name: %s, Index: %d, Declared: %s, Expected: %s)",
                            location,
                            name,
                            index,
                            arguments[index].getCanonicalName(),
                            required.getCanonicalName())
            );
    }

    private static <T extends Annotation> T search(Class<?> type, String name, Class<?>[] arguments, Class<T> anno)
    {
        Class<?> superClass = type.getSuperclass();

        if(superClass == null)
            return null;

       while(!superClass.equals(Object.class))
       {
           try {
               Method mthd = superClass.getDeclaredMethod(name, arguments);

               T annotation;
               if((annotation = mthd.getAnnotation(anno)) != null)
                   return annotation;
           } catch (NoSuchMethodException e) {
           }

           superClass = superClass.getSuperclass();
       }

        return null;
    }

    private static DataObjectMalformationException notInherited(String inheritance,
                                                                String annotation,
                                                                Class<?> type,
                                                                Method method)
    {
        return new DataObjectMalformationException(
                String.format("Not inherited from any %s but found %s (Class: %s, Method: %s)",
                        inheritance,
                        annotation,
                        type.getCanonicalName(),
                        method.toGenericString()));
    }

    private static DataObjectMalformationException incompatibleInheritance(String inheritance,
                                                                           String annotated,
                                                                           String current,
                                                                           Class<?> type,
                                                                           Method method)
    {
        return new DataObjectMalformationException(
                String.format("Incompatible %s inheritance (Class: %s, Method: %s, Annotated: %s, Current: %s)",
                        inheritance,
                        type.getCanonicalName(),
                        method.toGenericString(),
                        annotated,
                        current)
        );
    }

    private void parseMethods(Class<?> type, DataObjectContainer container, boolean inherited, boolean top)
            throws DataObjectInterpretationException
    {
        for(Method method : type.getDeclaredMethods())
        {
            MultiCondition.CurrentCondition<Class<?>> current;

            // annotations for checking
            current = annotationCondition.apply(
                    method,
                    InheritedGetter.class,
                    InheritedSetter.class
            );

            if(current.getCurrent().trueCount() != 0)
                current
                        .completelyOnlyIf(InheritedGetter.class)
                        .perform(() -> {
                            InheritedGetter inheritance = method.getAnnotation(InheritedGetter.class);
                            Getter getterInfo = search(type, method.getName(), method.getParameterTypes(), Getter.class);

                            if(getterInfo == null)
                                throw notInherited("getter", "@InheritedGetter", type, method);

                            if(!inheritance.value().isEmpty() && !inheritance.value().equals(getterInfo.value()))
                                throw incompatibleInheritance("getter", inheritance.value(), getterInfo.value(), type, method);
                        })
                        .elseCompletelyOnlyIf(InheritedSetter.class)
                        .perform(() -> {
                            InheritedSetter inheritance = method.getAnnotation(InheritedSetter.class);
                            Setter getterInfo = search(type, method.getName(), method.getParameterTypes(), Setter.class);

                            if(getterInfo == null)
                                throw notInherited("setter", "@InheritedSetter", type, method);

                            if(!inheritance.value().isEmpty() && !inheritance.value().equals(getterInfo.value()))
                                throw incompatibleInheritance("setter", inheritance.value(), getterInfo.value(), type, method);
                        })
                        .orElse(() -> {
                            throw new DataObjectMalformationException("Duplicated metadata (Method: " + method.toGenericString() + ") @InheritedGetter & @InheritedSetter");
                        });

            current = annotationCondition.apply(
                    method,
                    Setter.class,
                    Getter.class,
                    OverrideSetter.class,
                    OverrideGetter.class
            );

            if(current.getCurrent().trueCount() == 0)
                continue;

            current
                    .completelyOnlyIf(Getter.class)
                    .perform(() -> {
                        Getter getter = method.getAnnotation(Getter.class);
                        int modifier = method.getModifiers();

                        String name = getter.value();
                        ValueObjectContainer valueObjectContainer =
                                (ValueObjectContainer) container.getValueObject(name).orElseThrow(
                                        () -> new DataObjectMalformationException("Invalid getter: No such value object \"" + name + "\""));

                        if(valueObjectContainer.getter instanceof ValueObjectContainer.RedirectedGetter) // check duplication
                            throw new DataObjectMalformationException("Duplicated getter (Name: " + name + ")");

                        checkAndRedirectGetter(valueObjectContainer, type, method, name);
                    })
                    .elseCompletelyOnlyIf(Setter.class)
                    .perform(() -> {
                        Setter setter = method.getAnnotation(Setter.class);
                        int modifier = method.getModifiers();

                        String name = setter.value();
                        ValueObjectContainer valueObjectContainer =
                                (ValueObjectContainer) container.getValueObject(name).orElseThrow(
                                        () -> new DataObjectMalformationException("Invalid setter: No such value object \"" + name + "\""));

                        if(valueObjectContainer.setter instanceof ValueObjectContainer.RedirectedSetter) // check duplication
                            throw new DataObjectMalformationException("Duplicated setter (Name: " + name + ")");

                        checkAndRedirectSetter(valueObjectContainer, type, method, name);
                    })
                    .elseCompletelyOnlyIf(OverrideGetter.class)
                    .perform(() -> {
                        if(!inherited)
                            throw new DataObjectMalformationException("Using @OverrideGetter without @Inheritance");

                        OverrideGetter override = method.getAnnotation(OverrideGetter.class);
                        String overriding = override.value();

                        ValueObjectContainer valueObjectContainer =
                                (ValueObjectContainer) container.getValueObject(overriding).orElseThrow(
                                        () -> overridingFunctionOfNonexistentValueObject("getter", method, overriding));

                        if(valueObjectContainer.getter instanceof ValueObjectContainer.RedirectedGetter)
                            if(((ValueObjectContainer.RedirectedGetter) valueObjectContainer.getter).source().equals(type)) // check duplication
                                throw duplicatedOverrideFunction("getter", method, overriding);

                        checkAndRedirectGetter(valueObjectContainer, type, method, overriding);
                    })
                    .elseCompletelyOnlyIf(OverrideSetter.class)
                    .perform(() -> {
                        if(!inherited)
                            throw new DataObjectMalformationException("Using @OverrideSetter without @Inheritance");

                        OverrideSetter override = method.getAnnotation(OverrideSetter.class);
                        String overriding = override.value();

                        ValueObjectContainer valueObjectContainer =
                                (ValueObjectContainer) container.getValueObject(overriding).orElseThrow(
                                        () -> overridingFunctionOfNonexistentValueObject("getter", method, overriding));

                        if(valueObjectContainer.setter instanceof ValueObjectContainer.RedirectedSetter)
                            if(((ValueObjectContainer.RedirectedSetter) valueObjectContainer.setter).source().equals(type)) // check duplication
                                throw duplicatedOverrideFunction("setter", method, overriding);
                    })
                    .orElse(() -> {
                        throw new DataObjectMalformationException("Duplicated method metadata of " + method.toGenericString());
                    });
        }
    }

    private DataObjectMalformationException overridingFunctionOfNonexistentValueObject(String function, Method method, String overriding)
    {
       return new DataObjectMalformationException(
                String.format("Overriding %s of a non-existent value object " +
                                "(Method: %s, " +
                                "Overriding: %s)",
                        function,
                        method.toGenericString(),
                        overriding));
    }

    private DataObjectMalformationException duplicatedOverrideFunction(String function, Method method, String overriding)
    {
        return new DataObjectMalformationException(
                String.format("Duplicated %s " +
                                "(Overriding: %s, " +
                                "Type: %s)",
                        function,
                        overriding,
                        method.getDeclaringClass().getCanonicalName())
        );
    }

    private static void checkAndRedirectSetter(ValueObjectContainer valueObjectContainer, Class<?> type, Method method, String name)
            throws DataObjectInterpretationException
    {
        int modifier = method.getModifiers();

        if(Modifier.isStatic(modifier)) // static setter
        {
            Class<?>[] arguments = method.getParameterTypes();

            checkArgumentCount(arguments, 2, "Static Setter", name);

            checkArgument(arguments, 0, type, "Static Setter", name);
            checkArgument(arguments, 1, valueObjectContainer.getType(), "Static setter", name);

            method.setAccessible(true);
            valueObjectContainer.setter = new ValueObjectContainer.RedirectedSetterContainer(type,
                    (obj, val) -> {
                        try {
                            method.invoke(null, obj, val);
                        } catch (Exception e) {
                            throw new DataObjectError("Reflection error", e);
                        }
                    }
            );
        }
        else // non-static setter
        {
            Class<?>[] arguments = method.getParameterTypes();

            checkArgumentCount(arguments, 1, "Non-static Setter", name);

            checkArgument(arguments, 0, valueObjectContainer.getType(), "Non-static Setter", name);

            method.setAccessible(true);
            valueObjectContainer.setter = new ValueObjectContainer.RedirectedSetterContainer(type,
                    (obj, val) -> {
                        try {
                            method.invoke(obj, val);
                        } catch (Exception e) {
                            throw new DataObjectError("Reflection error", e);
                        }
                    }
            );
        }
    }

    private static void checkAndRedirectGetter(ValueObjectContainer valueObjectContainer, Class<?> type, Method method, String name)
            throws DataObjectInterpretationException
    {
        int modifier = method.getModifiers();

        if(!valueObjectContainer.getType().equals(method.getReturnType()))
            throw new DataObjectMalformationException("Invalid getter return type " +
                    "(Name: " + name + ", " +
                    "Declared: " + method.getReturnType().getCanonicalName() + ", " +
                    "Expected: " + valueObjectContainer.getType().getCanonicalName() + ")");

        if(Modifier.isStatic(modifier)) // static getter
        {
            Class<?>[] arguments = method.getParameterTypes();

            checkArgumentCount(arguments, 1, "Static Getter", name);

            checkArgument(arguments, 0, type, "Static Getter", name);

            method.setAccessible(true);
            valueObjectContainer.getter = new ValueObjectContainer.RedirectedGetterContainer(type,
                    (obj) -> {
                        try {
                            return method.invoke(null, obj);
                        } catch (Exception e) {
                            throw new DataObjectError("Reflection error", e);
                        }
                    }
            );
        }
        else // non-static getter
        {
            if(method.getParameterCount() != 0)
                throw new DataObjectMalformationException("Non-static getter should not have any argument (Name: " + name + ")");

            method.setAccessible(true);
            valueObjectContainer.getter = new ValueObjectContainer.RedirectedGetterContainer(type,
                    (obj) -> {
                        try {
                            return method.invoke(obj);
                        } catch (Exception e) {
                            throw new DataObjectError("Reflection error", e);
                        }
                    }
            );
        }
    }

    static NamedPredicate<AnnotatedElement, Class<?>> a(Class<? extends Annotation> annotation)
    {
        return NamedPredicate.of(annotation, (t) -> t.getAnnotation(annotation) != null);
    }

    private final Map<Class<?>, ExpandRule> bulitInRules;

    public static final StandardDataObjectInterpreter INSTANCE = new StandardDataObjectInterpreter();

    private static final MultiCondition<AnnotatedElement, Class<?>> annotationCondition =
            MultiCondition.of(MultiPredicate.<AnnotatedElement, Class<?>>builder()
                    .next(a(Unique.class))
                    .next(a(Multiple.class))
                    .next(a(Key.class))
                    .next(a(PrimaryKey.class))
                    .next(a(SecondaryKey.class))
//                  .next(a(Value.class))
                    .next(a(Getter.class))
                    .next(a(Setter.class))
                    .next(a(Inheritance.class))
                    .next(a(InheritedGetter.class))
                    .next(a(InheritedSetter.class))
//                  .next(a(BuiltinExpandRule.class))
//                  .next(a(CustomExpandRule.class))
//                  .next(a(Expandable.class))
                    .next(a(InheritValue.class))
                    .next(a(InheritKey.class))
                    .next(a(InheritPrimaryKey.class))
                    .next(a(InheritSecondaryKey.class))
                    .next(a(OverrideGetter.class))
                    .next(a(OverrideSetter.class))
            .build());

    private static interface DataObjectContainer extends DataObject
    {
        void putKey(KeyType type, ValueObject valueObject) throws DataObjectInterpretationException;

        void putValue(ValueObject valueObject) throws DataObjectInterpretationException;

        void seal() throws DataObjectInterpretationException; // except internal update

        boolean sealed();

        default void checkSeal()
        {
            if(sealed())
                throw new IllegalStateException("Already sealed");
        }
    }

    private static class GlobalExpandRules extends HashMap<Class<?>, ExpandRule>
    {
    }

    private static class UniqueDataObjectContainer implements UniqueDataObject, DataObjectContainer
    {
        UniqueDataObjectContainer(Class<?> type)
        {
            this.values = new HashMap<>();
            this.type = type;
        }

        @Override
        public void seal() throws DataObjectInterpretationException
        {
            this.checkSeal();

            if(this.key == null)
                throw new DataObjectMalformationException("Key not defined");

            this.values = Collections.unmodifiableMap(values);

            this.sealed = true;
        }

        @Override
        public boolean sealed()
        {
            return sealed;
        }

        @Override
        public ValueObject getKey()
        {
            return key;
        }

        @Override
        public Optional<ValueObject> getValue(String name)
        {
            return Optional.ofNullable(values.get(name));
        }

        @Override
        public Optional<ValueObject> getValueObject(String name)
        {
            if(name.equals(key.getName()))
                return Optional.of(key);

            return getValue(name);
        }

        @Override
        public Map<String, ValueObject> getValues()
        {
            return values;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        @Override
        public void putKey(KeyType type, ValueObject valueObject) throws DataObjectInterpretationException
        {
            this.checkSeal();

            if(!type.equals(KeyType.UNIQUE))
                throw new DataObjectMalformationException("@PrimaryKey and @SecondaryKey is not supported in unique data object");

            if(key != null)
                throw new DataObjectMalformationException("Primary key already exists");

            this.key = valueObject;
        }

        @Override
        public void putValue(ValueObject valueObject) throws DataObjectInterpretationException
        {
            this.checkSeal();

            if(key.getName().equals(valueObject.getName()) || (values.putIfAbsent(valueObject.getName(), valueObject) != null))
                throw new DataObjectMalformationException("Duplicated value object: " + valueObject.getName());
        }

        final Class<?> type;

        ValueObject key;

        Map<String, ValueObject> values;

        private boolean sealed;
    }

    private static class MultipleDataObjectContainer implements MultipleDataObject, DataObjectContainer
    {
        MultipleDataObjectContainer(Class<?> type)
        {
            this.type = type;
            this.secondaryKeys = new HashMap<>();
        }

        @Override
        public void putKey(KeyType type, ValueObject valueObject) throws DataObjectInterpretationException
        {
            this.checkSeal();

            switch(type)
            {
                case UNIQUE:
                    throw new DataObjectMalformationException("@Key is not supported in multiple data object (Or you/they should use @PrimaryKey maybe?)");

                case PRIMARY:
                    if(this.primaryKey != null)
                        throw new DataObjectMalformationException("Duplicated primary key");

                case SECONDARY:
                    if((secondaryKeys.putIfAbsent(valueObject.getName(), valueObject)) != null)
                        throw new DataObjectMalformationException("Duplicated secondary key: " + valueObject.getName());
            }
        }

        @Override
        public void putValue(ValueObject valueObject) throws DataObjectInterpretationException
        {
            this.checkSeal();

            if(primaryKey.getName().equals(valueObject.getName())
                    || secondaryKeys.containsKey(valueObject.getName())
                    || (values.putIfAbsent(valueObject.getName(), valueObject)) != null)
                throw new DataObjectMalformationException("Duplicated value object: " + valueObject.getName());
        }

        @Override
        public void seal() throws DataObjectInterpretationException
        {
            this.checkSeal();

            if(this.primaryKey == null)
                throw new DataObjectMalformationException("Primary Key not defined");

            this.secondaryKeys = Collections.unmodifiableMap(secondaryKeys);
            this.values = Collections.unmodifiableMap(values);

            this.sealed = true;
        }

        @Override
        public boolean sealed()
        {
            return sealed;
        }

        @Override
        public ValueObject getPrimaryKey()
        {
            return primaryKey;
        }

        @Override
        public Map<String, ValueObject> getSecondaryKeys()
        {
            return secondaryKeys;
        }

        @Override
        public Optional<ValueObject> getValue(String name)
        {
            return Optional.ofNullable(values.get(name));
        }

        @Override
        public Optional<ValueObject> getValueObject(String name)
        {
            if(name.equals(primaryKey.getName()))
                return Optional.of(primaryKey);

            ValueObject object;
            if((object = secondaryKeys.get(name)) != null)
                return Optional.of(object);

            return getValue(name);
        }

        @Override
        public Map<String, ValueObject> getValues()
        {
            return values;
        }

        @Override
        public Class<?> getType()
        {
            return type;
        }

        final Class<?> type;

        ValueObject primaryKey;

        Map<String, ValueObject> secondaryKeys;

        Map<String, ValueObject> values;

        private boolean sealed;
    }

    private static class ValueObjectContainer implements ValueObject
    {
        ValueObjectContainer(Class<?> owner, Class<?> type, Class<?> compatibleType)
        {
            this.ownerType = owner;
            this.type = type;
            this.compatibleType = compatibleType;
            this.metadata = new HashMap<>();
        }

        void seal() throws DataObjectInterpretationException
        {
            if(this.sealed)
                throw new IllegalStateException();

            if(name == null)
                throw new DataObjectMalformationException("Value name undefined");

            if(owner == null)
                throw new DataObjectMalformationException("null owner");

            if(getter == null)
                throw new DataObjectMalformationException("null getter");

            if(setter == null)
                throw new DataObjectMalformationException("null setter");

            if(primaryKey && expandRule != null)
                throw new DataObjectMalformationException("Expansion not allowed on primary keys (Name: " + name + ")");

            this.metadata = Collections.unmodifiableMap(this.metadata);

            this.sealed = true;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public Class<?> getType()
        {
            return this.type;
        }

        @Override
        public Object get(Object object)
        {
            Objects.requireNonNull(object, "object");

            return get0(object);
        }

        @Override
        public <T> T get(Object object, Class<T> type)
        {
            Objects.requireNonNull(object, "object");
            Objects.requireNonNull(type, "type");

            if(!this.ownerType.isInstance(object))
                throw DataObjectException.IncapableObject(object, this.ownerType);

            Object returned = get(object);

            if(returned == null)
                return null;

            if(!(type.isInstance(object)))
                throw new DataObjectError(DataObjectException.IncapableType(returned.getClass(), type));

            return (T) object;
        }

        Object get0(Object object)
        {
            return getter.get(object);
        }

        @Override
        public void set(Object object, Object value)
        {
            set(object, value, (Class) type);
        }

        @Override
        public <T> void set(Object object, T value, Class<T> type)
        {
            Objects.requireNonNull(object, "object");
            Objects.requireNonNull(type, "type");

            if(!this.ownerType.isInstance(object))
                throw DataObjectException.IncapableObject(object, this.ownerType);

            if(!type.isInstance(value) && (compatibleType == null || !compatibleType.isInstance(value)))
                throw DataObjectException.IncapableValue(value, type);

            set0(object, value);
        }

        void set0(Object object, Object value)
        {
            setter.set(object, value);
        }

        @Override
        public Class<?> getOwnerType()
        {
            return ownerType;
        }

        @Override
        public DataObject getOwner()
        {
            return owner;
        }

        @Override
        public boolean isPrimaryKey()
        {
            return primaryKey;
        }

        @Override
        public boolean isSecondaryKey()
        {
            return secondaryKey;
        }

        @Override
        public Optional<ExpandRule> getExpandRule()
        {
            return Optional.ofNullable(this.expandRule);
        }

        @Override
        public <T extends Annotation> Optional<T> getMetadata(Class<T> type)
        {
            return Optional.ofNullable((T) metadata.get(type));
        }

        @Override
        public Map<Class<?>, Annotation> getMetadataMap()
        {
            return Collections.unmodifiableMap(metadata);
        }

        final Class<?> ownerType;

        String name;

        final Class<?> type;

        final Class<?> compatibleType; // boxing

        boolean primaryKey;

        boolean secondaryKey;

        DataObject owner;

        ExpandRule expandRule;

        Getter getter;

        Setter setter;

        private Map<Class<?>, Annotation> metadata;

        private boolean sealed;

        private static interface Getter
        {
            Object get(Object object);
        }

        private static interface RedirectedGetter extends Getter, RedirectedElement
        {
        }

        private static interface Setter
        {
            void set(Object object, Object value);
        }

        private static interface RedirectedSetter extends Setter, RedirectedElement
        {
        }

        private static interface RedirectedElement
        {
            Class<?> source();
        }

        private static class RedirectedGetterContainer implements RedirectedGetter, RedirectedElement
        {
            RedirectedGetterContainer(Class<?> source, Getter getter)
            {
                this.source = source;
                this.getter = getter;
            }

            @Override
            public Object get(Object object)
            {
                return this.getter.get(object);
            }

            @Override
            public Class<?> source()
            {
                return this.source;
            }

            private final Class<?> source;

            private final Getter getter;
        }

        private static class RedirectedSetterContainer implements RedirectedSetter, RedirectedElement
        {
            RedirectedSetterContainer(Class<?> source, Setter setter)
            {
                this.source = source;
                this.setter = setter;
            }

            @Override
            public void set(Object object, Object value)
            {
                this.setter.set(object, value);
            }

            @Override
            public Class<?> source()
            {
                return this.source;
            }

            private final Class<?> source;

            private final Setter setter;
        }
    }

    private static class ExpandRuleContainer implements ExpandRule
    {
        ExpandRuleContainer(Class<?> type)
        {
            this.type = type;
        }

        void seal()
        {
            if(this.sealed)
                throw new IllegalStateException();

            this.entryList.toArray(this.entries = new Entry[this.entryList.size()]);
            this.entryList = null;

            this.sealed = true;
        }

        @Override
        public Class<?> getExpandingType()
        {
            return this.type;
        }

        @Override
        public Entry[] getEntries()
        {
            return Arrays.copyOf(entries, entries.length);
        }

        ArrayList<Entry> entryList = new ArrayList<>();

        final Class<?> type;

        private Entry[] entries;

        private boolean sealed;
    }

    private static class EntryContainer implements ExpandRule.Entry
    {
        EntryContainer(String name, Class<?> expandedType)
        {
            this.name = name;
            this.expandedType = expandedType;
            this.metadata = new HashMap<>();
        }

        void seal() throws DataObjectInterpretationException
        {
            if(this.sealed)
                throw new IllegalStateException();

            if(this.getterInfo == null)
                throw new DataObjectMalformationException("Getter info undefined");

            if(this.setterInfo == null)
                throw new DataObjectMalformationException("Setter info undefined");

            this.sealed = true;
        }

        @Override
        public String name()
        {
            return this.name;
        }

        @Override
        public ExpandRule.At getterInfo()
        {
            return this.getterInfo;
        }

        @Override
        public ExpandRule.At setterInfo()
        {
            return this.setterInfo;
        }

        @Override
        public Class<?> getExpandedType()
        {
            return this.expandedType;
        }

        @Override
        public <T extends Annotation> Optional<T> getMetadata(Class<T> type)
        {
            return Optional.ofNullable((T) metadata.get(type));
        }

        @Override
        public Map<Class<?>, Annotation> getMetadataMap()
        {
            return Collections.unmodifiableMap(metadata);
        }

        ExpandRule.At getterInfo;

        ExpandRule.At setterInfo;

        final Class<?> expandedType;

        final String name;

        private boolean sealed;

        private Map<Class<?>, Annotation> metadata;

        private static class AtInfo implements ExpandRule.At
        {
            AtInfo(String name, Source source)
            {
                this.name = name;
                this.source = source;
            }

            @Override
            public String name()
            {
                return this.name;
            }

            @Override
            public Source source()
            {
                return this.source;
            }

            final String name;

            final Source source;
        }
    }

    private static class InheritanceInfo
    {
        InheritanceInfo(InheritValue info)
        {
            this.name = info.name();
            this.field = info.field();
            this.source = info.source();
            this.type = info.type();
            this.strict = info.strict();
            this.expanding = info.expanding();
            this.genericString = toGenericString("@InheritValue", this);
        }


        InheritanceInfo(InheritKey info)
        {
            this.name = info.name();
            this.field = info.field();
            this.source = info.source();
            this.type = info.type();
            this.strict = info.strict();
            this.expanding = info.expanding();
            this.genericString = toGenericString("@InheritKey", this);
        }

        InheritanceInfo(InheritPrimaryKey info)
        {
            this.name = info.name();
            this.field = info.field();
            this.source = info.source();
            this.type = info.type();
            this.strict = info.strict();
            this.expanding = info.expanding();
            this.genericString = toGenericString("@InheritPrimaryKey", this);
        }

        InheritanceInfo(InheritSecondaryKey info)
        {
            this.name = info.name();
            this.field = info.field();
            this.source = info.source();
            this.type = info.type();
            this.strict = info.strict();
            this.expanding = info.expanding();
            this.genericString = toGenericString("@InheritSecondaryKey", this);
        }

        private static String toGenericString(String tag, InheritanceInfo info)
        {
            StringBuilder sb = new StringBuilder(tag);

            sb.append("(").append("field = \"").append(info.field()).append("\"");

            if(!info.name().isEmpty())
                sb.append(", name = \"").append(info.name()).append("\"");

            if(!info.source().equals(PlaceHolder.class))
                sb.append(", source = ").append(info.source().getCanonicalName()).append(".class");

            if(!info.type().equals(PlaceHolder.class))
                sb.append(", type = ").append(info.type().getCanonicalName()).append(".class");

            if(info.strict())
                sb.append(", strict = true");

            if(info.expanding().entries().length != 0)
                sb.append(", expanding = @Expandable(...)");

            sb.append(")");

            return sb.toString();
        }

        String name()
        {
            return this.name;
        }

        String field()
        {
            return this.field;
        }

        Class<?> source()
        {
            return this.source;
        }

        Class<?> type()
        {
            return this.type;
        }

        boolean strict()
        {
            return this.strict;
        }

        Expandable expanding()
        {
            return this.expanding;
        }

        @Override
        public String toString()
        {
            return genericString;
        }

        private final String genericString;

        private final String name;

        private final String field;

        private final Class<?> source;

        private final Class<?> type;

        private final boolean strict;

        private final Expandable expanding;
    }

    private static enum KeyType
    {
        UNIQUE,
        PRIMARY,
        SECONDARY
    }

    private static final Map<Class<?>, Class<?>> REFLECTION_COMPATIBLE_TYPES = new HashMap<Class<?>, Class<?>>()
    {
        {
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(char.class, Character.class);
            put(short.class, Short.class);
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(float.class, Float.class);
            put(double.class, Double.class);
        }
    };
}
