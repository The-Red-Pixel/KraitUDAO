# KraitUDAO
> Krait Universal Data Access Object  
> 超轻量级统一数据管理框架

## 数据对象类型

### 唯一数据对象 (UniqueDataObject)

只含有一个主键值对象的数据对象(在数据存储介质中只能存在一个此主键值对应的数据条目)。  
使用```@com.theredpixelteam.kraitudao.annotations.Unique```来注解数据对象类来表示该类所声明的数据对象为唯一数据对象（两种数据对象类型的注解不允许同时出现），如下：
```Java
@Unique
public class Example {
    // ... your declarations here
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
    
    // ... other declarations here
}
```
其中键值名称可以重新定义，如下：
```Java
@Unique
public class Example {
    @Key("KEY") // 不另取名称为 "KEY"
    private int exampleKey;
    
    // ... other declarations here
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
包含有一个主键值对象并且可以包含多个副键值对象的数据对象（在存储介质中一个主键值可以对应多个数据条目，而这些数据条目是跟据副键值来区分的，副键值不一定需要具有索引作用）  
使用```@com.theredpixelteam.kraitudao.annotations.Multiple```来注解数据对象类l来表示该类所声明的数据对象为可重复数据对象（两种数据对象类型的注解不允许同时出现），如下：
```Java
@Multiple
public class Example {
    // ... your declarations here
}
```
可重复数据对象支持一个主键对象与多个副键值对象，用以下其中一个来注解域：

- ```@com.theredpixelteam.kraitudao.annotations.PrimaryKey```
- ```@com.theredpixelteam.kraitudao.annotations.SecondaryKey``` 

并且可以有多个```@SecondaryKey```注解的域而只允许且必须有一个```@PrimaryKey```注解的域。不允许使用```@com.theredpixelteam.kraitudao.annotations.Key```来注解任何域。如下：
```Java
@Multiple
public class Example {
    @PrimaryKey
    private int primaryKey;
    
    @SecondaryKey
    priavte int secondaryKey1;
    
    @SecondaryKey
    private int secondaryKey2;
    
    // ... other declarations here
}
```
并且所有键与副键值都可以重新命名：
```Java
@Multiple
public class Example {
    @PrimaryKey("KEY0") // 更名为 "KEY0"
    private int primaryKey;
    
    @SecondaryKey("KEY1") // 更名为 "KEY1"
    priavte int secondaryKey1;
    
    @SecondaryKey("KEY2") // 更名为 "KEY2"
    private int secondaryKey2;
    
    // ... other declarations here
}
```
值对象必须使用```@com.theredpixelteam.kraitudao.annotations.Value```注解，使用规则与上文相同。

## 值与键值对象的命名

- 所有键值与键都默认使用其对应的域的名称，并且都是可以通过修改注解来重新命名的。  
- 所有值与键值对象的名称并不强制大小写敏感，这与框架的具体实现方式相关，因此不建议使用字母相同且仅有大小写不相同的名称。
- 请不要使用中文名称以导致各种奇怪的问题。

## 原生数据
通常情况下，原生数据包括以下类型：

- boolean *(java.lang.Boolean)*
- byte *(java.lang.Byte)*
- short *(java.lang.Short)*
- char *(java.lang.Character)*
- int *(java.lang.Integer)*
- long *(java.lang.Long)*
- float *(java.lang.Float)*
- double *(java.lang.Double)*
- java.lang.String
- java.math.BigDecimal

原生数据即不需要通过任何的处理，直接、必须由具体实现支持的，可以直接或以某种形式存储入存储介质中而不需要使用者预处理的数据类型。 本数据结构框架同时要求具体实现对于非原生数据的支持，但需要使用者指定处理方式，详见下文。

## 非原生数据类型的处理
对于非原生数据结构，需要使用者指定将其拆解为原生数据的方式，才能将其存储进存储介质之中。而使用者指定的这一方式我们称其为```ExpandRule```（扩展规则）。

### 内建扩展规则

未完待续

### 类全局扩展规则

未完待续

### 域扩展规则

未完待续

## 数据对象类继承

对于数据对象类，我们允许继承关系的存在，并且我们相信因此数据对象类的设计与操作会更加灵活。为了保证最少的误操作可能性，我们对继承的声明进行了严格的规定，大部分继承关系都需要注解声明。  
假设我们有一个唯一数据对象类，如下（可重复数据对象类的规定与下文的说明都相同，并且下文的说明都将基于这个例子）：
```Java
@Unique
public class House {
    @Key
    private int Lady;
    
    @Value
    private int Tom;
    
    private int Jerry;
}
```

你可能发现我们的小老鼠**Jerry**没有被标注为值对象，不要着急！下文它会发挥它的作用。  
有一天，**Tom**决定今天一定要抓住**Jerry**，趁**Jerry**不在家，**Tom**叫来了他的好朋友**Butch**，这时候家里便多了一只猫**Butch**。这时候我们不需要对我们的房子大动干戈，要表示多了一只猫的情况，我们只需要继承```House```即可，就像这样：
```Java
@Inheritance // 不要忘记这个！
@Unique
public class HouseWith2Cats extends House {
    @Value
    public int Butch;
}
```

这时候我们的数据对象里就包含了```Tom```和```Butch```两个值对象了。  
这个时候**Jerry**回来了，但是**Jerry**在这个房子里的地位是毫无疑问的，不需要重新声明，只需要像这样即可：
```Java
@Inheritance // 千万不要忘记这个！
@Unique
@InheriteValue(field = "Jerry" /*, name = "foo" */) // 你要是想的话，也可以给Jerry改个名字
public class HouseWith2CatsAnd1Mouse extends HouseWith2Cats {
    // 不需要其它操作
}
```

此时我们的数据对象里就包含了```Tom```,```Butch```和```Jerry```三个值对象了。  
**注意：使用```@InheriteValue```,```@InheriteKey```,```@InheritePrimaryKey```,```@InheriteSecondaryKey```时，对于继承的域的扫描方式，使用标准注解解析规则(```StandardDataObjectInterpreter```)时，规定如果存在继承关系，且没有使用其他参数，则从当前类向超类扫描，找到第一个符合名称的域为止。若没有找到，则会抛出解析错误。**

### 附加参数

* **```source``` 用来指定域所在的类，若该类不存在此域，或此类不在继承关系中，则抛出解析错误。**  
例如：
```Java
public class ExampleA {
    private int value;
}

public class ExampleB extends ExampleA {
    private int value;
}

@Unique
@InheriteValue(field = "value", source = ExampleA.class)
public class Example extends ExampleB {
    @Key
    private int key;
}
```
如果我们置```source = ExampleA.class```，则继承的值域为```ExampleA```中的```value```，否则由于自下向上扫描的机制，继承的值域为```ExampleB```中的```value```。而若```ExampleA```中不存在```value```域，则会抛出解析错误。

* **```strict``` 如果置```strict = true```则会严格检查继承关系中的```@Inheritance```注解，反之不严格检查。**  
例如如下继承关系：
```Java
public class ExampleBase {
    private int value;
}

@Unique
@InheriteValue(field = "value" /*, strict = false*/)
public class Example extends ExampleBase {
    // ...
}
```
在这种情况下，如果我们置```strict = true```：
```Java
@Unique
@InheriteValue(field = "value", strict = true)
public class Example extends Example Base {
    // ...
}
```
则会抛出解析错误，由于域```value```存在于父类```ExampleBase```中，但```Exmaple```并没有使用```@Inheritance```进行标注，并且父类并不是一个标准的数据对象类。简言之，在```strict = true```时，如果扫描域时，正在扫描的类没有被```@Inheritance```注解，则扫描过程会在当前被扫描的类停止，若```strict = false```（默认）则会继续扫描。  
以下示例中就不会因此抛出解析错误：
```Java
@Unique
public class ExampleA {
    @Key
    private int key;
    
    private int value;
}

@Unique
@Inheritance
@InheriteValue(field = "value", strict = true)
public class Example {
    // ...
}
```

未完待续