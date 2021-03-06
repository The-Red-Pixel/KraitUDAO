/*
 * PlainSQLDatabaseDataSource.java
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

package com.theredpixelteam.kraitudao.common;

import com.theredpixelteam.kraitudao.*;
import com.theredpixelteam.kraitudao.annotations.Element;
import com.theredpixelteam.kraitudao.annotations.metadata.common.*;
import com.theredpixelteam.kraitudao.common.sql.*;
import com.theredpixelteam.kraitudao.dataobject.*;
import com.theredpixelteam.kraitudao.dataobject.util.ValueObjectIterator;
import com.theredpixelteam.kraitudao.interpreter.DataObjectExpander;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpretationException;
import com.theredpixelteam.kraitudao.interpreter.DataObjectInterpreter;
import com.theredpixelteam.kraitudao.interpreter.DataObjectMalformationException;
import com.theredpixelteam.kraitudao.interpreter.common.StandardDataObjectExpander;
import com.theredpixelteam.kraitudao.interpreter.common.StandardDataObjectInterpreter;
import com.theredpixelteam.redtea.function.*;
import com.theredpixelteam.redtea.util.*;
import com.theredpixelteam.redtea.util.Optional;
import com.theredpixelteam.redtea.util.concurrent.Increment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("unchecked")
public class PlainSQLDatabaseDataSource implements DataSource {
    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectExpander expander,
                                      DataObjectContainer container,
                                      DatabaseManipulator databaseManipulator,
                                      DataArgumentWrapper argumentWrapper,
                                      DataExtractorFactory extractorFactory)
            throws DataSourceException
    {
        this.connection = connection;
        this.tableName = tableName;
        this.interpreter = interpreter;
        this.expander = expander;
        this.container = container;
        this.manipulator = databaseManipulator;
        this.argumentWrapper = argumentWrapper;
        this.extractorFactory = extractorFactory;

        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectExpander expander,
                                      DataObjectContainer container,
                                      DatabaseManipulator databaseManipulator)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, expander, container, databaseManipulator, DefaultDataArgumentWrapper.INSTANCE, DefaultDataExtractorFactory.INSTANCE);
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectExpander expander,
                                      DataObjectContainer container)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, expander, container, H2DatabaseManipulator.INSTANCE);
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName,
                                      DataObjectInterpreter interpreter,
                                      DataObjectExpander expander)
            throws DataSourceException
    {
        this(connection, tableName, interpreter, expander, DataObjectCache.getGlobal());
    }

    public PlainSQLDatabaseDataSource(Connection connection,
                                      String tableName)
            throws DataSourceException
    {
        this(connection, tableName, StandardDataObjectInterpreter.INSTANCE, StandardDataObjectExpander.INSTANCE);
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public String getTableName()
    {
        return this.tableName;
    }

    private void checkTransaction(Transaction transaction) throws DataSourceException
    {
        if((transaction == null && this.currentTransaction != null)
                || (transaction != null && !transaction.equals(this.currentTransaction)))
            throw new DataSourceException.Busy();
    }

    private static DataSourceException typeUnsupportedByArgumentWrapper(Class<?> type)
    {
        return new DataSourceException.UnsupportedValueType("(Caused by ArgumentWrapper) " + type.getCanonicalName());
    }

    private static DataSourceException typeUnsupportedByExtractor(Class<?> type)
    {
        return new DataSourceException.UnsupportedValueType("(Caused by Extractor) " + type.getCanonicalName());
    }

    private static DataSourceException typeUnsupportedAsMapValue(Class<?> type)
    {
        return new DataSourceException.UnsupportedValueType("(As Map value) " + type.getCanonicalName());
    }

    private static DataSourceException typeUnsupportedAsListElement(Class<?> type)
    {
        return new DataSourceException.UnsupportedValueType("(As List element) " + type.getCanonicalName());
    }

    private static DataSourceException duplicatedAnnotation(Class<?> type)
    {
        return new DataSourceException(new DataObjectMalformationException("Duplicated metadata annotation in class: " + type.getCanonicalName()));
    }

    private static DataSourceException vagueCollectionType()
    {
        return new DataSourceException(new DataObjectMalformationException("Vague collection type (Multi-implementation)"));
    }

    private void checkForKeyToken(Class<?> type) throws DataSourceException
    {
        if (!manipulator.supportType(type))
            throw new DataSourceException("Unsupported key type: " + type.getCanonicalName());
    }

    private void checkForValueToken(Class<?> type) throws DataSourceException
    {
    }

    private static int getCollectionType(Class<?> type) throws DataSourceException
    {
        return (List.class.isAssignableFrom(type) ? TYPE_LIST : 0)
                | (Set.class.isAssignableFrom(type) ? TYPE_SET : 0)
                | (Map.class.isAssignableFrom(type) ? TYPE_MAP : 0);
    }

    private static boolean isVagueCollectionType(int type)
    {
        return (type > 0b010 && type != 0b100);
    }

    private static int checkCollectionType(Class<?> type) throws DataSourceException
    {
        int i = getCollectionType(type);

        if (i == 0)
            throw new DataSourceException(type.getCanonicalName() + " is not a collection type");

        if (isVagueCollectionType(i))
            throw vagueCollectionType();

        return i;
    }

    private static Class<?> getSignature(Class<?>[] signature, Increment signaturePointer) throws DataSourceException
    {
        int index = signaturePointer.getAndInc();

        if (index < signature.length)
            return signature[index];

        throw new DataSourceException(new DataObjectMalformationException("Uncompleted signature"));
    }

    private void extract(ResultSet resultSet, Object object, ValueObject valueObject)
            throws DataSourceException
    {
        extract(resultSet, object, valueObject, Prefix.of(), null, null);
    }

    private void extract(ResultSet resultSet, Object object, ValueObject valueObject, Prefix prefix, Class<?>[] signature, Increment signaturePointer)
            throws DataSourceException
    {
        Object value = valueObject.get(object);
        ObjectConstructor<?> constructor = valueObject.getConstructor();

        if (!constructor.onlyOnNull() || value == null) try {
            valueObject.set(object, value = constructor.newInstance(object));
        } catch (Exception e) {
            throw new DataSourceException("Construction failure", e);
        }

        switch (valueObject.getStructure())
        {
            case VALUE:
                extractValue(resultSet, object, valueObject, prefix);
                break;

            case MAP:
                if (signature == null)
                {
                    ValueMap valueMap = valueObject.getMetadata(ValueMap.class)
                            .orElseThrow(() -> new DataSourceException(
                                    new DataObjectMalformationException("Missing metadata @ValueMap (Name: " + valueObject.getName() + ")")));

                    signature = valueMap.signatured();
                    signaturePointer = new Increment();
                }

                final Map<Object, Object> map;

                try {
                    map = (Map) value;
                } catch (ClassCastException e) {
                    throw new DataSourceError(e);
                }

                extractMap(resultSet, map::put, valueObject.getName(), prefix, signature, signaturePointer);
                break;

            case SET:
                if (signature == null)
                {
                    ValueSet valueSet = valueObject.getMetadata(ValueSet.class)
                            .orElseThrow(() -> new DataSourceException(
                                    new DataObjectMalformationException("Missing metadata @ValueSet (Name: " + valueObject.getName() + ")")));

                    signature = valueSet.signatured();
                    signaturePointer = new Increment();
                }

                final Set<Object> set;

                try {
                    set = (Set) value;
                } catch (ClassCastException e) {
                    throw new DataSourceError(e);
                }

                extractSet(resultSet, set::add, valueObject.getName(), prefix, signature, signaturePointer);
                break;

            case LIST:
                if (signature == null)
                {
                    ValueList valueList = valueObject.getMetadata(ValueList.class)
                            .orElseThrow(() -> new DataSourceException(
                                    new DataObjectMalformationException("Missing metadata @ValueList (Name: " + valueObject.getName() + ")")));

                    signature = valueList.signatured();
                    signaturePointer = new Increment();
                }

                final List<Object> list;

                try {
                    list = (List) value;
                } catch (ClassCastException e) {
                    throw new DataSourceError(e);
                }

                extractList(resultSet, list::add, valueObject.getName(), prefix, signature, signaturePointer);
                break;

            default:
                throw new ShouldNotReachHere();
        }
    }

    private <K, V> void extractMap(ResultSet resultSet,
                                   BiConsumerWithThrowable<K, V, ? extends Throwable> put,
                                   String column,
                                   Prefix prefix,
                                   Class<?>[] signature,
                                   Increment signaturePointer) throws DataSourceException
    {
        try {
            Class<K> mapKeyType = (Class<K>) getSignature(signature, signaturePointer);
            Class<V> mapValueType = (Class<V>) getSignature(signature, signaturePointer);

            checkForKeyToken(mapKeyType);
            checkForValueToken(mapValueType);

            if (column != null)
            {
                String mapTableIdentity = (String) extractorFactory.create(String.class, asCollectionColumnName(prefix.apply(column)))
                        .orElseThrow(() -> typeUnsupportedByExtractor(String.class))
                        .extract(resultSet);

                resultSet = manipulator.query(connection, asCollectionTableName(mapTableIdentity), null, null);
            }

            while (resultSet.next())
            {
                K mapKeyObject = (K) extractRaw(resultSet, mapKeyType,  "K");
                V mapValueObject;

                ThreeStateOptional<DataObjectType> dataObjectTypeOptional = interpreter.getDataObjectType(mapValueType)
                        .throwIfNull(() -> duplicatedAnnotation(mapValueType));

                if (dataObjectTypeOptional.isPresent()) // @Element
                {
                    DataObjectType dataObjectType = dataObjectTypeOptional.getSilently();

                    if (dataObjectType.ordinal() > 0)
                        throw typeUnsupportedAsMapValue(mapValueType);

                    ElementDataObject mapValueDataObject;
                    try {
                        mapValueDataObject = (ElementDataObject) container.interpretIfAbsent(mapValueType, interpreter);
                    } catch (ClassCastException | DataObjectInterpretationException e) {
                        throw new DataSourceException("Exception occurred when interpreting element data object in map value", e);
                    }

                    try {
                        mapValueObject = (V) mapValueDataObject.getConstructor().newInstance(null);
                    } catch (Exception e) {
                        throw new DataSourceException("Exception occurred when constructing element data object in map value", e);
                    }

                    for (ValueObject elementValueObject : mapValueDataObject.getValues().values())
                        extract(resultSet, mapValueObject, elementValueObject, MAP_VALUE_PREFIX, signature, signaturePointer);
                }
                else
                    mapValueObject = (V) extractRaw(resultSet, mapValueType, "V", Prefix.of(), signature, signaturePointer);

                try {
                    put.accept(mapKeyObject, mapValueObject);
                } catch (Throwable e) {
                    throw new DataSourceException("Exception occurred when putting elements into the map", e);
                }
            }
        } catch (SQLException | ClassCastException e) {
            throw new DataSourceException(e);
        }
    }

    private <E> void extractSet(ResultSet resultSet,
                                ConsumerWithThrowable<E, ? extends Throwable> add,
                                String column,
                                Prefix prefix,
                                Class<?>[] signature,
                                Increment signaturePointer) throws DataSourceException
    {
        try {
            Class<E> setElementType = (Class<E>) getSignature(signature, signaturePointer);

            checkForKeyToken(setElementType);

            if (column != null)
            {
                String setTableIdentity = (String) extractorFactory.create(String.class, asCollectionColumnName(prefix.apply(column)))
                        .orElseThrow(() -> typeUnsupportedByExtractor(String.class))
                        .extract(resultSet);

                resultSet = manipulator.query(connection, asCollectionTableName(setTableIdentity), null, null);
            }

            while (resultSet.next())
            {
                E setElementObject = (E) extractRaw(resultSet, setElementType, "E");

                try {
                    add.accept(setElementObject);
                } catch (Throwable e) {
                    throw new DataSourceException("Exception occurred when putting elements into the set", e);
                }
            }
        } catch (SQLException | ClassCastException e) {
            throw new DataSourceException(e);
        }
    }

    private <E> void extractList(ResultSet resultSet,
                                 ConsumerWithThrowable<E, ? extends Throwable> add,
                                 String column,
                                 Prefix prefix,
                                 Class<?>[] signature,
                                 Increment signaturePointer) throws DataSourceException
    {
        try {
            Class<E> listElementType = (Class<E>) getSignature(signature, signaturePointer);

            checkForValueToken(listElementType);

            if (column != null)
            {
                String listTableIdentity = (String) extractorFactory.create(String.class, asCollectionColumnName(prefix.apply(column)))
                        .orElseThrow(() -> typeUnsupportedByExtractor(String.class))
                        .extract(resultSet);

                resultSet = manipulator.query(connection, asCollectionTableName(listTableIdentity), null, null);
            }

            while (resultSet.next())
            {
                E listElementObject;

                ThreeStateOptional<DataObjectType> dataObjectTypeOptional = interpreter.getDataObjectType(listElementType)
                        .throwIfNull(() -> duplicatedAnnotation(listElementType));

                if (dataObjectTypeOptional.isPresent())
                {
                    DataObjectType dataObjectType = dataObjectTypeOptional.getSilently();

                    if (dataObjectType.ordinal() > 0)
                        throw typeUnsupportedAsListElement(listElementType);

                    ElementDataObject elementDataObject;
                    try {
                        elementDataObject = (ElementDataObject) container.interpretIfAbsent(listElementType, interpreter);
                    } catch (ClassCastException | DataObjectInterpretationException e) {
                        throw new DataSourceException("Exception occurred when interpreting element data object in list element", e);
                    }

                    try {
                        listElementObject = (E) elementDataObject.getConstructor().newInstance(null);
                    } catch (Exception e) {
                        throw new DataSourceException("Exception occurred when constructing element data object in list element", e);
                    }

                    for (ValueObject elementValueObject : elementDataObject.getValues().values())
                        extract(resultSet, listElementObject, elementValueObject, LIST_ELEMENT_PREFIX, signature, signaturePointer);
                }
                else
                    listElementObject = (E) extractRaw(resultSet, listElementType, "E", Prefix.of(), signature, signaturePointer);

                try {
                    add.accept(listElementObject);
                } catch (Throwable e) {
                    throw new DataSourceException("Exception occurred when putting elements into the list", e);
                }
            }
        } catch (SQLException | ClassCastException e) {
            throw new DataSourceException(e);
        }
    }

    private void extractValue(ResultSet resultSet, Object object, ValueObject valueObject, Prefix prefix) throws DataSourceException
    {
        Class<?> dataType = valueObject.getType();

        boolean supported = manipulator.supportType(dataType);
        boolean expandForcibly = valueObject.hasMetadata(ExpandForcibly.class);

        boolean expanding = expandForcibly || !supported;

        EXPANDABLE_OBJECT_CONSTRUCTION:
        if (expanding)
        {
            if (valueObject.get(object) != null)
                break EXPANDABLE_OBJECT_CONSTRUCTION;

            try {
                // Return type of the constructor should be verified during the interpretation
                valueObject.set(object, valueObject.getConstructor().newInstance(object));
            } catch (Exception e) {
                throw new DataSourceException("Construction failure", e);
            }
        }

        boolean elementAnnotated = dataType.getAnnotation(Element.class) != null;

        if (!expandForcibly && elementAnnotated) try // EXPAND_ELEMENT_VALUE_OBJECT
        {
            DataObject dataObject = container.interpretIfAbsent(dataType, interpreter);

            if (!DataObjectType.ELEMENT.equals(dataObject.getDataObjectType()))
                throw new DataSourceException.UnsupportedValueType(dataType.getCanonicalName());

            ElementDataObject elementDataObject = (ElementDataObject) dataObject;
            Prefix nextPrefix = prefix.append(valueObject.getName());

            for (ValueObject elementValueObject : elementDataObject.getValues().values())
                extract(resultSet, object, elementValueObject, nextPrefix, null, null);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
        else if (expanding) try // EXPAND_EXPANDABLE_VALUE_OBJECT
        {
            Map<String, ValueObject> expanded = container.expand(valueObject, expander)
                    .orElseThrow(() -> new DataSourceException.UnsupportedValueType(dataType.getCanonicalName()));

            Prefix nextPrefix = prefix.append(valueObject.getName());

            for (ValueObject expandedValueObject : expanded.values())
                extract(resultSet, object, expandedValueObject, nextPrefix, null, null);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
        else
            valueObject.set(object, extractRaw(resultSet, dataType, prefix.apply(valueObject.getName())));
    }

    // including collection types
    private Object extractRaw(ResultSet resultSet,
                              Class<?> dataType,
                              String columnName,
                              Prefix prefix,
                              Class<?>[] signature,
                              Increment signaturePointer)
            throws DataSourceException
    {
        EXTRACT_COLLECTION: try
        {
            int i = getCollectionType(dataType);

            if (i == 0)
                break EXTRACT_COLLECTION;

            if (isVagueCollectionType(i))
                throw vagueCollectionType();

            Object object;
            try {
                object = dataType.newInstance();
            } catch (Exception e) {
                throw new DataSourceException("Object construction failure", e);
            }

            extractCollection(resultSet, i, object, columnName, prefix, signature, signaturePointer);

            return object;
        } catch (ClassCastException e) {
            throw new DataSourceError(e);
        }

        return extractRaw(resultSet, dataType, prefix.apply(columnName));
    }

    // primitive types only
    private Object extractRaw(ResultSet resultSet,
                              Class<?> dataType,
                              String columnName) throws DataSourceException
    {
        DataExtractor extractor = extractorFactory.create(dataType, columnName)
                .orElseThrow(() -> typeUnsupportedByExtractor(dataType));

        Object value;
        try {
            value = extractor.extract(resultSet);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

        if (!dataType.isInstance(value))
            throw new DataSourceError("Extraction failure (Bad type)");

        return value;
    }

    private void extractCollection(ResultSet resultSet,
                                   int type,
                                   Object object,
                                   String column,
                                   Prefix prefix,
                                   Class<?>[] signature,
                                   Increment signaturePointer) throws DataSourceException
    {
        switch (type)
        {
            case TYPE_LIST: // List
                final List<Object> list;

                try {
                    list = (List) object;
                } catch (ClassCastException e) {
                    throw new DataSourceError(e);
                }

                extractList(resultSet, list::add, column, prefix, signature, signaturePointer);

                break;

            case TYPE_SET: // Set
                final Set<Object> set;

                try {
                    set = (Set) object;
                } catch (ClassCastException e) {
                    throw new DataSourceError(e);
                }

                extractSet(resultSet, set::add, column, prefix, signature, signaturePointer);

                break;

            case TYPE_MAP: // Map
                final Map<Object, Object> map;

                try {
                    map = (Map) object;
                } catch (ClassCastException e) {
                    throw new DataSourceError(e);
                }

                extractMap(resultSet, map::put, column, prefix, signature, signaturePointer);

                break;

            default:
                throw new ShouldNotReachHere();
        }
    }

    private <T, X extends Throwable> void extractAll(ResultSet resultSet,
                                                     DataObject dataObject,
                                                     SupplierWithThrowable<T, X> constructor,
                                                     Consumer<T> consumer)
            throws DataSourceException
    {
        try {
            while (resultSet.next())
            {
                T object;
                try {
                    object = constructor.get();
                } catch (Throwable e) {
                    throw new DataSourceException("Object construction failure", e);
                }

                for (ValueObject valueObject : new ValueObjectIterator(dataObject))
                    extract(resultSet, object, valueObject);

                consumer.accept(object);
            }
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }

    private static String[] valuesExceptKeys(DataObject dataObject)
    {
        Set<String> valueSet = dataObject.getValues().keySet();
        return valueSet.toArray(new String[0]);
    }

    @Override
    public <T> boolean pull(T object, Class<T> type) throws DataSourceException
    {
        try {
            DataObject dataObject = container.interpretIfAbsent(type, interpreter);

            Pair<String, DataArgument>[] keys;
            String[] values;

            DataObjectType dataObjectType = dataObject.getDataObjectType();
            switch (dataObjectType)
            {
                case ELEMENT:
                    throw new DataSourceException("Element data object is not allowed in global scope");

                case UNIQUE:
                    UniqueDataObject uniqueDataObject = (UniqueDataObject) dataObject;
                    ValueObject key = uniqueDataObject.getKey();

                    Object keyValue = key.get(object);

                    if (keyValue == null)
                        throw new DataSourceException("(pull) Null key \"" + key.getName() + "\" in UniqueDataObject");

                    values = valuesExceptKeys(dataObject);
                    keys = new Pair[]{Pair.of(key.getName(), argumentWrapper.wrap(keyValue)
                            .orElseThrow(() -> typeUnsupportedByArgumentWrapper(key.getType())))};

                    break;

                case MULTIPLE:

                    MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
                    ValueObject primaryKey = multipleDataObject.getPrimaryKey();
                    Collection<ValueObject> secondaryKeys = multipleDataObject.getSecondaryKeys().values();

                    List<Pair<String, DataArgument>> keyList = new ArrayList<>();

                    Object primaryKeyValue = primaryKey.get(object);

                    if (primaryKeyValue == null)
                        throw new DataSourceException("(pull) Null primary key \"" + primaryKey.getName() + "\" in MultipleDataObject");

                    keyList.add(Pair.of(primaryKey.getName(), argumentWrapper.wrap(primaryKeyValue)
                            .orElseThrow(() -> typeUnsupportedByArgumentWrapper(primaryKey.getType()))));

                    for (ValueObject secondaryKey : secondaryKeys) {
                        Object secondaryKeyValue = secondaryKey.get(object);

                        if (secondaryKeyValue == null)
                            throw new DataSourceException("(pull) Null secondary key \"" + secondaryKey.getName() + "\" in MultipleDataObject");

                        keyList.add(Pair.of(secondaryKey.getName(), argumentWrapper.wrap(secondaryKeyValue)
                                .orElseThrow(() -> typeUnsupportedByArgumentWrapper(secondaryKey.getType()))));
                    }

                    values = valuesExceptKeys(dataObject);
                    keys = keyList.toArray(new Pair[0]);

                    break;

                default:
                    throw new DataSourceError("Interpretation failure");
            }

            try (ResultSet resultSet = manipulator.query(connection, tableName, keys, values)) {
                if (!resultSet.next())
                    return false;

                for (ValueObject valueObject : dataObject.getValues().values())
                    extract(resultSet, object, valueObject);
            } catch (SQLException e) {
                throw new DataSourceException(e);
            }

            return true;
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public <T, X extends Throwable> Collection<T> pull(Class<T> type, SupplierWithThrowable<T, X> constructor) throws DataSourceException
    {
        Collection<T> collection = new ArrayList<>();

        String[] values;
        DataObject dataObject;

        try {
            dataObject = container.interpretIfAbsent(type, interpreter);

            if (DataObjectType.ELEMENT.equals(dataObject.getDataObjectType()))
                throw new DataSourceException("Element data object is not allowed in global scope");

            List<String> valueList = new ArrayList<>();
            for (ValueObject valueObject : new ValueObjectIterator(dataObject))
                valueList.add(valueObject.getName());

            values = valueList.toArray(new String[0]);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }

        try (ResultSet resultSet = manipulator.query(connection, tableName, null, values)) {
            extractAll(resultSet, dataObject, constructor, collection::add);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

        return collection;
    }

    @Override
    public <T, X extends Throwable> T pull(Class<T> type, SupplierWithThrowable<T, X> constructor, Class<?>... signatures)
            throws DataSourceException
    {
        int i = checkCollectionType(type);

        T object;
        try {
            object = constructor.get();
        } catch (Throwable e) {
            throw new DataSourceException(e);
        }

        try (ResultSet resultSet = manipulator.query(connection, tableName, null, null)) {
            extractCollection(resultSet, i, object, null, Prefix.of(), signatures, new Increment());
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

        return object;
    }

    @Override
    public <T, X extends Throwable> Collection<T> pullVaguely(T object, Class<T> type, SupplierWithThrowable<T, X> constructor)
            throws DataSourceException
    {
        Collection<T> collection = new ArrayList<>();

        String[] values;
        Pair<String, DataArgument>[] keys;
        DataObject dataObject;

        try {
            dataObject = container.interpretIfAbsent(type, interpreter);

            if (!DataObjectType.MULTIPLE.equals(dataObject.getDataObjectType()))
                throw new DataSourceException("Only multiple data object allowed in this scope");

            List<Pair<String, DataArgument>> keyList = new ArrayList<>();
            List<String> valueList = new ArrayList<>();

            for (ValueObject valueObject : new ValueObjectIterator(dataObject))
            {
                Object value = valueObject.get(object);

                if (!valueObject.isKey() || value == null)
                    valueList.add(valueObject.getName());
                else
                    keyList.add(Pair.of(valueObject.getName(), argumentWrapper.wrap(value)
                            .orElseThrow(() -> typeUnsupportedByArgumentWrapper(value.getClass()))));
            }

            keys = keyList.toArray(new Pair[0]);
            values = valueList.toArray(new String[0]);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }

        try (ResultSet resultSet = manipulator.query(connection, tableName, keys, values)) {
            extractAll(resultSet, dataObject, constructor, collection::add);
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }

        return collection;
    }

    private void cleanupCollection(String identity) throws DataSourceException
    {
        List<String> collections = new ArrayList<>();

        stepintoCollections(identity == null ? tableName : asCollectionTableName(identity), collections);

        if (collections.isEmpty())
            return;

        try {
            manipulator.cleanTable(connection, collections.toArray(new String[0]));
        } catch (SQLException e) {
            throw new DataSourceException("Cleanup", e);
        }
    }

    private void stepintoCollections(String root, List<String> collections) throws DataSourceException
    {
        List<String> collectionColumns = new ArrayList<>();

        try (ResultSet resultSet = manipulator.queryTop(connection, root, null, null, 1)) {
            if (!resultSet.next())
                return;

            ResultSetMetaData metadata = resultSet.getMetaData();
            int count = metadata.getColumnCount();

            for (int i = 0; i < count;)
            {
                String name = metadata.getColumnName(++i);

                if (isCollectionColumnName(name))
                    collectionColumns.add(name);
            }
        } catch (SQLException e) {
            throw new DataSourceException("Prequery", e);
        }

        if (collectionColumns.isEmpty())
            return;

        try (ResultSet resultSet = manipulator.query(connection, root, null, collectionColumns.toArray(new String[0]))) {
            while (resultSet.next())
                for (String column : collectionColumns)
                {
                    String id = resultSet.getString(column);

                    if (id != null)
                        cleanupCollection(id);
                }

            collections.add(root);
        } catch (SQLException e) {
            throw new DataSourceException("Query", e);
        }
    }

    private void commit(Object object, ValueObject valueObject, List<Pair<String, DataArgument>> values, Prefix prefix)
            throws DataSourceException
    {
        Class<?> valueType = valueObject.getType();

        switch (valueObject.getStructure())
        {
            case VALUE:
                commitValue(object, valueObject, values, prefix);
                break;

            case LIST:
            case SET:
            case MAP:

                break;
        }
    }

    private void commitList(Object object, ValueObject valueObject, List<Pair<String, DataArgument>> values, Prefix prefix)
            throws DataSourceException
    {
        String rootIdentity = prefix.apply(valueObject.getName());
        String column = asCollectionColumnName(valueObject.getName());

        cleanupCollection(rootIdentity);

        String collectionIdentity = generateCollectionIdentity(tableName);
        String collectionTable = asCollectionTableName(collectionIdentity);

        values.add(Pair.of(column, argumentWrapper.wrap(collectionIdentity)
                .orElseThrow(() -> typeUnsupportedByArgumentWrapper(String.class))));

//        manipulator.createTable(connection, collectionIdentity);
        // TODO
    }

    private void commitValue(Object object, ValueObject valueObject, List<Pair<String, DataArgument>> values, Prefix prefix)
            throws DataSourceException
    {
        Class<?> type = valueObject.getType();
        Object value = valueObject.get(object);

        boolean supported = manipulator.supportType(type);
        boolean expandForcibly = valueObject.hasMetadata(ExpandForcibly.class);

        boolean elementAnnotated = type.getAnnotation(Element.class) != null;

        boolean expanding = expandForcibly || supported;

        if (elementAnnotated || expanding) try {
            Iterable<ValueObject> iterable;

            if (!expandForcibly && elementAnnotated)
            {
                DataObject dataObject = container.interpretIfAbsent(type, interpreter);

                if (!DataObjectType.ELEMENT.equals(dataObject.getDataObjectType()))
                    throw typeUnsupportedByArgumentWrapper(type);

                iterable = new ValueObjectIterator((ElementDataObject) dataObject);
            }
            else
                iterable = container.expand(valueObject, expander)
                        .orElseThrow(() -> typeUnsupportedByArgumentWrapper(type))
                        .values();

            Prefix nextPrefix = prefix.append(valueObject.getName());

            for (ValueObject expandedValueObject : iterable)
                commit(value, expandedValueObject, values, nextPrefix);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
        else
        {
            if (value == null)
                if (valueObject.isKey())
                    throw new DataSourceException("Key cannot be null");
                else if (valueObject.isNotNull())
                    throw new DataSourceException("@NotNull declared but null value presented");

            values.add(Pair.of(prefix.apply(valueObject.getName()), argumentWrapper.wrap(value)
                    .orElseThrow(() -> typeUnsupportedByArgumentWrapper(type))));
        }
    }

    @Override
    public <T> Transaction commit(Transaction transaction, T object, Class<T> type)
            throws DataSourceException
    {
        try {
            DataObject dataObject = container.interpretIfAbsent(type, interpreter);

            if (dataObject instanceof ElementDataObject)
                throw new DataSourceException("Element data object is not allowed in global scope");


        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }

        return null;
    }

    @Override
    public <T> Transaction commit(Transaction transaction, T object, Class<T> type, Class<?>... signatures)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction remove(Transaction transaction, T object, Class<T> type)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public Transaction clear(Transaction transaction) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction removeVaguely(Transaction transaction, T object, Class<T> type)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public void waitForTransaction()
    {
        while(this.currentTransaction != null);
    }

    public void createTable(Connection conection, Class<?> dataType) throws DataSourceException
    {
        try {
            createTable0(connection, container.interpretIfAbsent(dataType, interpreter), false);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
    }

    public void createTable(Connection connection, DataObject dataObject) throws DataSourceException
    {
        createTable0(connection, dataObject, false);
    }

    public boolean createTableIfNotExists(Connection connection, Class<?> dataType) throws DataSourceException
    {
        try {
            return createTable0(connection, container.interpretIfAbsent(dataType, interpreter), true);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
    }

    public boolean createTableIfNotExists(Connection connection, DataObject dataObject) throws DataSourceException
    {
        return createTable0(connection, dataObject, true);
    }

    private boolean createTable0(Connection connection, DataObject dataObject, boolean ifNotExists) throws DataSourceException
    {
        return createTable0(connection, tableName, dataObject, ifNotExists);
    }

    @SuppressWarnings("unchecked")
    private boolean createTable0(Connection connection, String tableName, DataObject dataObject, boolean ifNotExists) throws DataSourceException
    {
        try {
            List<Constraint> tableConstraints = new ArrayList<>();
            List<Vector3<String, Class<?>, Constraint[]>> columns = new ArrayList<>();

            List<ValueObject> keys = new ArrayList<>();

            List<ValueObject> valueObjects = new ArrayList<>();
            for (ValueObject valueObject : new ValueObjectIterator(dataObject))
            {
                Class<?> columnType = tryRemapping(valueObject.getType());

                if (valueObject.hasMetadata(ExpandForcibly.class) || !manipulator.supportType(columnType))
                    valueObjects.addAll(container.expand(valueObject, expander)
                            .orElseThrow(() -> new DataSourceException.UnsupportedValueType(columnType.getCanonicalName())).values());
                else
                    valueObjects.add(valueObject);

                for (ValueObject confirmed : valueObjects)
                {
                    if(confirmed.isKey())
                        keys.add(confirmed);

                    columns.add(Vector3.of(
                            confirmed.getName(),
                            tryRemapping(confirmed.getType()),
                            confirmed.hasMetadata(NotNull.class) ? new Constraint[]{Constraint.ofNotNull()} : new Constraint[0]));
                }

                valueObjects.clear();
            }

            if(!keys.isEmpty())
            {
                String[] keyNames = new String[keys.size()];

                for(int i = 0; i < keyNames.length; i++)
                    keyNames[i] = keys.get(i).getName();

                tableConstraints.add(Constraint.ofPrimaryKey(keyNames));
            }

            Constraint[] tableConstraintArray = tableConstraints.toArray(new Constraint[0]);
            Vector3<String, Class<?>, Constraint[]>[] columnArray = columns.toArray(new Vector3[0]);

            if(ifNotExists)
                return manipulator.createTableIfNotExists(connection, tableName, columnArray, tableConstraintArray);
            else
                manipulator.createTable(connection, tableName, columnArray, tableConstraintArray);
        } catch (DataObjectInterpretationException | SQLException e) {
            throw new DataSourceException(e);
        }

        return true;
    }

    public DatabaseManipulator getManipulator()
    {
        return manipulator;
    }

    public void setManipulator(DatabaseManipulator manipulator)
    {
        this.manipulator = Objects.requireNonNull(manipulator);
    }

    public DataArgumentWrapper getArgumentWrapper()
    {
        return argumentWrapper;
    }

    public void setArgumentWrapper(DataArgumentWrapper argumentWrapper)
    {
        this.argumentWrapper = Objects.requireNonNull(argumentWrapper);
    }

    public void setExtractorFactory(DataExtractorFactory extractorFactory)
    {
        this.extractorFactory = Objects.requireNonNull(extractorFactory);
    }

    public DataExtractorFactory getExtractorFactory()
    {
        return extractorFactory;
    }

    private static Class<?> tryRemapping(Class<?> type)
    {
        if (Map.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type))
            return String.class;

        return type;
    }

    private String asCollectionTableName(String identity)
    {
        return tableName + COLLECTION_TABLE_SUFFIX + identity;
    }

    private static String asCollectionColumnName(String column)
    {
        return column + COLLECTION_COLUMN_SUFFIX;
    }

    private static boolean isCollectionColumnName(String column)
    {
        return column.endsWith(COLLECTION_COLUMN_SUFFIX);
    }

    private static String generateCollectionIdentity(String tableName)
    {
        return UUID.randomUUID().toString().replace('-', '_');
    }

    private volatile Transaction currentTransaction;

    protected String tableName;

    protected Connection connection;

    protected DataObjectInterpreter interpreter;

    protected DataObjectExpander expander;

    protected final DataObjectContainer container;

    protected DatabaseManipulator manipulator;

    protected DataArgumentWrapper argumentWrapper;

    protected DataExtractorFactory extractorFactory;

    private static final String COLLECTION_TABLE_SUFFIX = "_XXSYNTHETIC_COLLECTION_TABLE_";

    private static final String COLLECTION_COLUMN_SUFFIX = "_XXSYNTHETIC_TAG_COLLECTION";

    private static final Prefix MAP_VALUE_PREFIX = Prefix.of("V");

    private static final Prefix LIST_ELEMENT_PREFIX = Prefix.of("E");

    private static final int TYPE_LIST = 0b001;

    private static final int TYPE_SET = 0b010;

    private static final int TYPE_MAP = 0b100;

    private class TransactionImpl implements Transaction
    {
        TransactionImpl()
        {
        }

        @Override
        public boolean push() throws DataSourceException
        {
            if(!valid)
                return false;

            try {
                connection.commit();
            } catch (SQLException e) {
                throw new DataSourceException(e);
            }

            destroy();
            return true;
        }

        @Override
        public boolean cancel()
        {
            if(!valid)
                return false;

            try {
                connection.rollback();
            } catch (SQLException e) {
                this.lastException = new DataSourceException(e);
                return false;
            }

            destroy();
            return true;
        }

        @Override
        public Optional<Exception> getLastException()
        {
            return Optional.ofNullable(this.lastException);
        }

        void destroy()
        {
            this.valid = false;
            currentTransaction = null;
        }

        boolean valid()
        {
            return valid;
        }

        private Exception lastException;

        private boolean valid = true;
    }

    private static class Prefix
    {
        static Prefix of()
        {
            return EMPTY;
        }

        static Prefix of(String... contents)
        {
            if (contents.length == 0)
                return of();

            StringBuilder sb = new StringBuilder(contents[0]);
            for (int i = 1; i < contents.length; i++)
                sb.append("_").append(contents[i]);

            return new Prefix(sb.toString());
        }

        private Prefix(String prefix)
        {
            this.prefix = prefix;
        }

        @Override
        public String toString()
        {
            return prefix;
        }

        public String apply(String string)
        {
            return prefix.isEmpty() ? string : prefix + "_" + string;
        }

        public Prefix append(String prefix)
        {
            return new Prefix(apply(prefix));
        }

        private final String prefix;

        private static final Prefix EMPTY = new EmptyPrefix();

        private static final class EmptyPrefix extends Prefix
        {
            private EmptyPrefix()
            {
                super("");
            }

            @Override
            public String toString()
            {
                return "";
            }

            @Override
            public String apply(String string)
            {
                return string;
            }

            @Override
            public Prefix append(String prefix)
            {
                return Prefix.of(prefix);
            }
        }
    }
}
