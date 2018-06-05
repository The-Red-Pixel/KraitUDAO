package com.theredpixelteam.kraitudao;

import java.util.Collection;

/**
 * 版权所有（C） The Red Pixel <theredpixelteam.com>
 * 版权所有（C)  KuCrO3 Studio
 * 这一程序是自由软件，你可以遵照自由软件基金会出版的GNU通用公共许可证条款
 * 来修改和重新发布这一程序。或者用许可证的第二版，或者（根据你的选择）用任
 * 何更新的版本。
 * 发布这一程序的目的是希望它有用，但没有任何担保。甚至没有适合特定目的的隐
 * 含的担保。更详细的情况请参阅GNU通用公共许可证。
 */
@SuppressWarnings("unchecked")
public interface DataSource {
    public default void pull(Object object) throws DataSourceException
    {
        pull(object, (Class) object.getClass());
    }

    public <T> void pull(T object, Class<T> type) throws DataSourceException;

    public <T> Collection<T> pull(Class<T> type) throws DataSourceException;

    public <T> Collection<T> pullVaguely(T object) throws DataSourceException;

    public default Transaction commit(Collection<Object> objects) throws DataSourceException
    {
        return commit(null, objects);
    }

    public Transaction commit(Transaction transition, Collection<Object> objects) throws DataSourceException;

    public default <T> Transaction commit(T object, Class<T> type) throws DataSourceException
    {
        return commit(null, object, type);
    }

    public <T> Transaction commit(Transaction transition, T object, Class<T> type) throws DataSourceException;

    public default <T> Transaction remove(T object) throws DataSourceException
    {
        return remove(null, object);
    }

    public <T> Transaction remove(Transaction transition, T object) throws DataSourceException;

    public default <T> Transaction clear() throws DataSourceException
    {
        return clear(null);
    }

    public <T> Transaction clear(Transaction transition) throws DataSourceException;

    public default <T> Transaction removeVaguely(T object) throws DataSourceException
    {
        return removeVaguely(null, object);
    }

    public <T> Transaction removeVaguely(Transaction transition, T object) throws DataSourceException;

    public void waitForTransaction();
}
