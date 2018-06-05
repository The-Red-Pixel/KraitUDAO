package com.theredpixelteam.kraitudao.interpreter;

/**
 * 版权所有（C） The Red Pixel <theredpixelteam.com>
 * 版权所有（C)  KuCrO3 Studio
 * 这一程序是自由软件，你可以遵照自由软件基金会出版的GNU通用公共许可证条款
 * 来修改和重新发布这一程序。或者用许可证的第二版，或者（根据你的选择）用任
 * 何更新的版本。
 * 发布这一程序的目的是希望它有用，但没有任何担保。甚至没有适合特定目的的隐
 * 含的担保。更详细的情况请参阅GNU通用公共许可证。
 */
public class DataObjectMalformationException extends DataObjectInterpretationException {
    public DataObjectMalformationException()
    {
    }

    public DataObjectMalformationException(String msg)
    {
        super(msg);
    }

    public DataObjectMalformationException(Throwable cause)
    {
        super(cause);
    }

    public DataObjectMalformationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
