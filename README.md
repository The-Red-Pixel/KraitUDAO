# KraitUDAO
> 超轻量级统一数据管理框架

## 数据对象类型

### 唯一数据对象 (UniqueDataObject)

只含有一个主键值对象的数据对象(在数据存储介质中只能存在一个此主键值对应的数据条目)。  
使用```@com.theredpixelteam.kraitudao.annotations.Unique```来注解数据对象类则表示该类所声明的数据对象为唯一数据对象（两种数据对象类型的注解不允许同时出现），如下：
```Java
@Unique
public class Example {
    ... // your declarations here
}
```
而其主键必须使用```@com.theredpixelteam.kraitudao.annotations.Key```注解  
并且规定唯一数据对象中只含有一个主键，不允许出现副键。故以下两种注解：

- ```@com.theredpixelteam.kraitudao.annotations.PrimaryKey```
- ```@com.theredpixelteam.kraitudao.annotations.SecondaryKey```   

中的任何一种都不允许出现在一个唯一数据对象类中。如下：
```Java
@Unique
public class Example {
    @Key // 不另取名称，主键值在存储介质中即名为 "exampleKey"
    private int exampleKey;
    
    ... // other declarations here
}
```
其中键值名称可以重新定义，如下：
```Java
@Unique
public class Example {
    @Key("KEY") // 不另取名称为 "KEY"
    private int exampleKey;
    
    ... // other declarations here
}
```
值对象必须使用```@com.theredpixelteam.kraitudao.annotations.Value```注解，一个简单的唯一数据对象类应为如下形式：
```Java
@Unique
public class Example {
    @Key
    private int exampleKey;
    
    @Value
    private int exampleValue;
}
```
同样，值对象也可以重新定义名称：
```Java
@Unique
public class Example {
    @Key("KEY") 更名为 "KEY"
    private int exampleKey;
    
    @Value("VALUE") // 更名为 "VALUE"
    private int exampleValue;
}
```

### 可重复数据对象 (MultipleDataObject)

## 值与键值对象的命名
- 所有键值与键都默认使用其对应的域的名称，并且都是可以通过修改注解来重新命名的。  
- 所有值与键值对象的名称并不强制大小写敏感，这与框架的具体实现方式相关，因此不建议使用字母相同且仅有大小写不相同的名称。
- 请不要使用中文名称以导致各种奇怪的问题。