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
import com.theredpixelteam.redtea.function.Supplier;
import com.theredpixelteam.redtea.function.SupplierWithThrowable;
import com.theredpixelteam.redtea.util.Pair;
import com.theredpixelteam.redtea.util.ShouldNotReachHere;
import com.theredpixelteam.redtea.util.Vector3;
import com.theredpixelteam.redtea.util.concurrent.Increment;
import com.theredpixelteam.redtea.util.Optional;

import java.sql.Connection;
import java.sql.ResultSet;
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
        this(connection, tableName, interpreter, expander, container, DefaultDatabaseManipulator.INSTANCE);
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

    private static Supplier<DataSourceException> typeUnsupportedByArgumentWrapper(Class<?> type)
    {
        return () -> new DataSourceException.UnsupportedValueType("(Caused by ArgumentWrapper) " + type.getCanonicalName());
    }

    private static Supplier<DataSourceException> typeUnsupportedByExtractor(Class<?> type)
    {
        return () -> new DataSourceException.UnsupportedValueType("(Caused by Extractor) " + type.getCanonicalName());
    }

    private void checkForKeyToken(Class<?> type) throws DataSourceException
    {
        if (!manipulator.supportType(type))
            throw new DataSourceException("Unsupported key type: " + type.getCanonicalName());
    }

    private void checkForValueToken(Class<?> type) throws DataSourceException
    {
    }

    private static Class<?> getSignature(Class<?>[] signature, Increment signaturePointer) throws DataSourceException
    {
        int index = signaturePointer.getAndInc();

        if (index < signature.length)
            return signature[index];

        throw new DataSourceException(new DataObjectMalformationException("Uncompleted signature"));
    }

    private void extract(ResultSet resultSet, Object object, ValueObject valueObject, StringBuilder prefix, Class<?>[] signature, Increment signaturePointer)
            throws DataSourceException
    {
        Class<?> dataType;
        switch (valueObject.getStructure())
        {
            case VALUE:
                extractValue(resultSet, object, valueObject, new StringBuilder());
                break;

            case MAP:
                if (signature == null)
                {
                    ValueMap valueMap = valueObject.getMetadata(ValueMap.class)
                            .orElseThrow(() -> new DataSourceException(
                                    new DataObjectMalformationException("Missing metadata @ValueMap (Name: " + valueObject.getName() + ")")));
                    dataType = valueObject.getType();

                    if (!dataType.isAssignableFrom(valueMap.type()))
                        throw new DataSourceException(
                                new DataObjectMalformationException("Illegal type in @ValueMap (Name: " + valueObject.getName() + ")"));

                    signature = valueMap.signatured();
                    signaturePointer = new Increment();
                }

                extractMap(resultSet, object, valueObject.getName(), prefix, signature, signaturePointer);
                break;

            case SET:
                if (signature == null)
                {
                    ValueSet valueSet = valueObject.getMetadata(ValueSet.class)
                            .orElseThrow(() -> new DataSourceException(
                                    new DataObjectMalformationException("Missing metadata @ValueSet (Name: " + valueObject.getName() + ")")));
                    dataType = valueObject.getType();

                    if (!dataType.isAssignableFrom(valueSet.type()))
                        throw new DataSourceException(
                                new DataObjectMalformationException("Illegal type in @ValueSet (Name: " + valueObject.getName() + ")"));

                    signature = valueSet.signatured();
                    signaturePointer = new Increment();
                }

                extractSet(resultSet, object, valueObject, prefix, signature, signaturePointer);
                break;

            case LIST:
                if (signature == null)
                {
                    ValueList valueList = valueObject.getMetadata(ValueList.class)
                            .orElseThrow(() -> new DataSourceException(
                                    new DataObjectMalformationException("Missing metadata @ValueList (Name: " + valueObject.getName() + ")")));
                    dataType = valueObject.getType();

                    if (!dataType.isAssignableFrom(valueList.type()))
                        throw new DataSourceException(
                                new DataObjectMalformationException("Illegal type in @ValueList (Name: " + valueObject.getName() + ")"));

                    signature = valueList.signatured();
                    signaturePointer = new Increment();
                }

                extractList(resultSet, object, valueObject, prefix, signature, signaturePointer);
                break;

            default:
                throw new ShouldNotReachHere();
        }
    }

    private void extractMap(ResultSet resultSet, Object object, String column, StringBuilder prefix,
                            Class<?>[] signature, Increment signaturePointer) throws DataSourceException
    {
        try {
            if (!resultSet.next())
                return;

            String mapTableIdentity = (String) extractorFactory.create(String.class, column)
                    .orElseThrow(() -> new DataSourceError("STRING is not supported by extractor"))
                    .extract(resultSet);

            String mapTableName = tableName + "_XXSYNTHETIC_MAP_" + mapTableIdentity;

            Class<?> mapKeyType = signature[signaturePointer.getAndInc()];
            Class<?> mapValueType = signature[signaturePointer.getAndInc()];

            checkForKeyToken(mapKeyType);

            // TODO

        } catch (SQLException | ClassCastException e) {
            throw new DataSourceException(e);
        }
    }

    private void extractSet(ResultSet resultSet, Object object, ValueObject valueObject, StringBuilder prefix,
                            Class<?>[] signature, Increment signaturePointer) throws DataSourceException
    {

    }

    private void extractList(ResultSet resultSet, Object object, ValueObject valueObject, StringBuilder prefix,
                             Class<?>[] signature, Increment signaturePointer) throws DataSourceException
    {

    }

    private <T> void extractValue(ResultSet resultSet, Object object, ValueObject valueObject, StringBuilder prefix) throws DataSourceException
    {
        Class<T> dataType = (Class<T>) valueObject.getType();

        boolean supported = manipulator.supportType(dataType);
        boolean expandForcibly = valueObject.hasMetadata(ExpandForcibly.class);

        boolean expanding = expandForcibly || !supported;

        EXPANDABLE_OBJECT_CONSTRUCTION:
        if (expanding)
        {
            if (valueObject.get(object) != null)
                break EXPANDABLE_OBJECT_CONSTRUCTION;

            ObjectConstructor<T> objectConstructor = valueObject.getConstructor(dataType)
                    .throwIfEmpty(
                            () -> new DataSourceException(
                                    new DataObjectMalformationException("Bad constructor type of value object \"" + valueObject.getName() + "\"")))
                    .getWhenNull(() -> ObjectConstructor.ofDefault(dataType));

            try {
                T constructed = objectConstructor.newInstance(object);

                valueObject.set(object, constructed);
            } catch (Exception e) {
                throw new DataSourceException("Construction failure", e);
            }
        }

        boolean elementAnnotated = valueObject.getType().getAnnotation(Element.class) != null;

        if (!expandForcibly && elementAnnotated) try // EXPAND_ELEMENT_VALUE_OBJECT
        {
            DataObject dataObject = container.interpretIfAbsent(dataType, interpreter);

            if (!(dataObject instanceof ElementDataObject))
                throw new DataSourceException.UnsupportedValueType(dataType.getCanonicalName());

            ElementDataObject elementDataObject = (ElementDataObject) dataObject;

            for (ValueObject elementValueObject : elementDataObject.getValues().values())
                extract(resultSet, object, elementValueObject, prefix.append(valueObject.getName()).append("_"), null, null);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
        else if (expanding) try // EXPAND_EXPANDABLE_VALUE_OBJECT
        {
            Map<String, ValueObject> expanded = container.expand(valueObject, expander)
                    .orElseThrow(() -> new DataSourceException.UnsupportedValueType(dataType.getCanonicalName()));

            for (ValueObject expandedValueObject : expanded.values())
                extract(resultSet, object, expandedValueObject, prefix.append(valueObject.getName()).append("_"), null, null);
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
        else
            valueObject.set(object, extract0(resultSet, dataType, prefix.toString(), valueObject.getName()));
    }

    private Object extract0(ResultSet resultSet, Class<?> dataType, String prefix, String columnName) throws DataSourceException
    {
        DataExtractor extractor = extractorFactory.create(dataType, prefix + columnName)
                .orElseThrow(typeUnsupportedByExtractor(dataType));

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

    private static String[] valuesExceptKeys(DataObject dataObject)
    {
        Set<String> valueSet = dataObject.getValues().keySet();
        return valueSet.toArray(new String[valueSet.size()]);
    }

    @Override
    public <T> boolean pull(T object, Class<T> type) throws DataSourceException
    {
        try {
            DataObject dataObject = container.interpretIfAbsent(type, interpreter);

            Pair<String, DataArgument>[] keys;
            String[] values;

            if (dataObject instanceof ElementDataObject)
            {
                values = valuesExceptKeys(dataObject);
                keys = null;
            }
            else if (dataObject instanceof UniqueDataObject)
            {
                UniqueDataObject uniqueDataObject = (UniqueDataObject) dataObject;
                ValueObject key = uniqueDataObject.getKey();

                Object keyValue = key.get(object);

                if (keyValue == null)
                    throw new DataSourceException("(pull) Null key \"" + key.getName() + "\" in UniqueDataObject");

                values = valuesExceptKeys(dataObject);
                keys = new Pair[] {Pair.of(key.getName(), argumentWrapper.wrap(keyValue)
                            .orElseThrow(typeUnsupportedByArgumentWrapper(key.getType())))};
            }
            else if (dataObject instanceof MultipleDataObject)
            {
                MultipleDataObject multipleDataObject = (MultipleDataObject) dataObject;
                ValueObject primaryKey = multipleDataObject.getPrimaryKey();
                Collection<ValueObject> secondaryKeys = multipleDataObject.getSecondaryKeys().values();

                List<Pair<String, DataArgument>> keyList = new ArrayList<>();

                Object primaryKeyValue = primaryKey.get(object);

                if (primaryKeyValue == null)
                    throw new DataSourceException("(pull) Null primary key \"" + primaryKey.getName() + "\" in MultipleDataObject");

                keyList.add(Pair.of(primaryKey.getName(), argumentWrapper.wrap(primaryKeyValue)
                        .orElseThrow(typeUnsupportedByArgumentWrapper(primaryKey.getType()))));

                for (ValueObject secondaryKey : secondaryKeys)
                {
                    Object secondaryKeyValue = secondaryKey.get(object);

                    if (secondaryKeyValue == null)
                        throw new DataSourceException("(pull) Null secondary key \"" + secondaryKey.getName() + "\" in MultipleDataObject");

                    keyList.add(Pair.of(secondaryKey.getName(), argumentWrapper.wrap(secondaryKeyValue)
                            .orElseThrow(typeUnsupportedByArgumentWrapper(secondaryKey.getType()))));
                }

                values = valuesExceptKeys(dataObject);
                keys = keyList.toArray(new Pair[keyList.size()]);
            }
            else
                throw new DataSourceError("Interpretation failure");

            try (ResultSet resultSet = manipulator.query(connection, tableName, keys, values)) {
                if (!resultSet.next())
                    return false;

                for (ValueObject valueObject : dataObject.getValues().values())
                        extract(resultSet, object, valueObject, new StringBuilder(), null, null);
            } catch (SQLException e) {
                throw new DataSourceException(e);
            }

            return true;
        } catch (DataObjectInterpretationException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public <T> boolean pull(T object, Class<T> type, Class<?>... signatured) throws DataSourceException
    {
        return false;
    }

    @Override
    public <T, X extends Throwable> Collection<T> pull(Class<T> type, SupplierWithThrowable<T, X> constructor) throws DataSourceException
    {
        return null;
    }

    @Override
    public <T, X extends Throwable> Collection<T> pull(Class<T> type, SupplierWithThrowable<T, X> constructor, Class<?>... signatured)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public <T, X extends Throwable> Collection<T> pullVaguely(T object, Class<T> type, SupplierWithThrowable<T, X> constructor)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction commit(Transaction transaction, T object, Class<T> type)
            throws DataSourceException
    {
        return null;
    }

    @Override
    public <T> Transaction commit(Transaction transaction, T object, Class<T> type, Class<?>... signatured)
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

            Constraint[] tableConstraintArray = tableConstraints.toArray(new Constraint[tableConstraints.size()]);
            Vector3<String, Class<?>, Constraint[]>[] columnArray = columns.toArray(new Vector3[columns.size()]);

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

    private volatile Transaction currentTransaction;

    protected String tableName;

    protected Connection connection;

    protected DataObjectInterpreter interpreter;

    protected DataObjectExpander expander;

    protected final DataObjectContainer container;

    protected DatabaseManipulator manipulator;

    protected DataArgumentWrapper argumentWrapper;

    protected DataExtractorFactory extractorFactory;

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

    private static class KeyInjection
    {
        KeyInjection()
        {
        }

        void inject(Object object)
        {
            for(Pair<ValueObject, Object> entry : injectiveElements)
                entry.first().set(object, entry.second());
        }

        private final List<Pair<ValueObject, Object>> injectiveElements = new ArrayList<>();
    }
}
