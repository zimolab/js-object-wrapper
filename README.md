## 项目说明

   一个使用编译时注解处理器从kotlin接口自动生成JavaScript对象包装代码的库。

  在JavaFx中，我们可以利用WebView组件加载网页，通过WebEngine执行JavaScript脚本，并且可以在Javascript与Java（Kotlin）之间进行交互。
这种交互，体现在两个方面，一是从Java中调用Javascript中的对象（函数），二则是相反的过程，即从Javascript代码中调用Java中的对象。

  WebView提供的渲染能力，WebEngine提供的代码执行以及在宿主环境与脚本环境之间进行通信和交互的能力，使得我们完全可以利用它们来进行混合开发，这样做的好处是，可以利用web世界（特别是前端）已有的库和工具来构建我们的应用程序，一定程度上避免重复造轮子。

  在对一些规模较小的JavaScript库进行封装时，直接利用WebEngine、JSObject提供的接口进行开发是可行的。然而，当规模开始扩大时，事情就变得有一点复杂了，我们可能会发现，我们将不得不编写大量高度重复性的代码，而且无法通过简单的软件工程的方法（例如设计模式）来简化这一过程，事情开始变得非常十分枯燥乏味，而且极其容易出错。不仅如此，最终的代码可能也会变得难以维护的。

  举例来说。如果我们想要在Java（这里以kotlin代码为例）中调用JavaScript中的一个Date对象，那么代码可能是这样的：

```javascript
// js中的对象
var date_in_js = new Date()
data_in_js.myProperty = "hello"
```

```kotlin

import netscape.javascript.JSObject
import java.util.*

// 首先获得js中的对象，jsDate变量的类型为JSObject
// JSObject代表了js中的一个对象
val jsDate: JSObject = webEngine.executeScript("date_in_js") as JSObject
// 假设我们想要调用Javascript Date对象的setTime(millisec)方法，那么我们需要使用JsObject.call()方法
val time = Date().time
jsDate.call("setTime", time)
// 如果我们想要获取或者设置js对象的属性（字段），那么我们则需要借助JSObject.getMember()/JSObject.setMember()方法
// 在上面的Js代码中，我们在date_in_js对象上添加了一个myProperty字段，现在在java中读写这个字段
val myProperty = jsDate.getMember("myProperty") // 读取字段
// 在使用这个值之前，我们需要做一些检查，确保调用已经成功
if (myProperty !is String) {
    throw RuntimeException("can not get member of js object")
}
println("jsDate.myProperty=$myProperty")
// 写字段
val newValue = "world"
jsDate.setMember("myProperty", newValue)
```

  上述代码是我们在Java中调用JS对象的基本模式，我们首先获取JS对象在Java层面的一个引用（JSObject对象），然后调用getMember()、setMember()方法来读写JS对象的属性，通过call()方法来调用JS对象的方法。

  你可能注意到了，这种做法的存在的问题：我们是通过使用字符串形式的属性名和方法名来调用JS对象的相应方法和属性的，这和我们平时使用点语法来调用对象属性、方法的形式相距甚远，不仅十分麻烦、非常违反对象化编程的习惯，而且极大的增加了出错的可能性——毕竟字符串中的typo不仅是非常常见的，而且无法将这个错误提前到编译期来。
  有没有办法像使用Java对象一样来调用JS对象呢？当然可以，而且十分简单，用Java类封装一下就行。

```kotlin

 import netscape.javascript.JSObject
 class JsDate(val source: JSObject) {
     
     var myProperty: String
        set(value) {
            source.setMember("myProperty", value)
        }
        get() {
            val ret = source.getMember("myProperty")
            if (ret !is String)
                throw RuntimeException("can not get member of js object")
            return ret
        }
     
     fun setTime(millisec: Long) {
         source.call("setTime", millisec)
     }
     
 }
 val dateInJs: JSObject = webEngine.executeScript("date_in_js") as JSObject 
 val jsDate = JsDate(dateInJs)
 println(jsDate.myProperty)
 jsDate.myProperty = "world"
 jsDate.setTime(Date().time)
```
  上面的代码是不是就自然多了。你可能会想，按照这种模式，将JS对象的函数、属性统统封装起来也并不困难。然而，事情远远没有那么简单。
当我们真正尝试去相对完整地去封装一个JS对象时，我们很快就会发现，我们需要编写大量重复的代码，就以JS中的Date对象为例，它有几十个方法，想象一下，
那是多么大的工作量。如果一个系统中，还不止这一个对象呢？所以有时候，编程算得上是真正的体力劳动。

  当然，手动编写代码的问题还不止于此。在上面的代码中，其实还存在一个小小的bug。在JS中，date.setTime()接收的是一个long类型的参数，
而根据JavaFx中关于Java与Javascript之间类型映射的描述，可以发现，WebEngine实际上无法将Java中long型的数据自动映射到Javascript中，


|  Javascript类型   | Java类型        |
|  ----------------| -------------- |
| string           | String         |
| bool            | Boolean         |
| number          | Integer或Double |
| undefine        | "undefined"    |
| 其他Object       | JSObject      |

  因此，在上面的代码中，如果我们在调用JsDate.setTime()时给了一个非常大的值（足以溢出），那么很可能造成某些难以察觉而且奇奇怪怪的bug。

  可能的解决办法是使用类型映射，即在调用setMember()之前，将无法由WebEngine自动映射的参数，通过自定义的方法映射成JS能够正确理解的值。代码可能是这样的：

```kotlin

 import netscape.javascript.JSObject
 
 class JsDate(val source: JSObject) {
    //...这里省略其他代码
    
    fun mappingLong(value: Long): Any {
        val ret = source.eval("{a=$value}")
        if(ret == null || ret == "undefined")
            throw RuntimeException("....")
        return ret
    }
    
     fun setTime(millisec: Long) {
         source.call("setTime", mappingLong(millisec))
     }
     
 }
```

  虽然需要处理Java之间JS之间类型映射的问题，但这个问题的解决看上去好像并不困难。在参数数量和类型都比较少的情况下确实如此。
但如果我们考虑到存在大量参数或者（以及）参数类型比较复杂的情形时，事情就不像看上去那么简单了。这候，需要考虑哪些参数需要映射，哪些不需要以及可变长参数的处理等等一系列问题。
另一种有关类型映射的典型情形是，函数接收另外一个JS对象作为参数的情形，或者返回一个JS对象，此时就需要考虑：
如何在调用时将JS对象的Java包装类映射到JS中，以及在返回时，如何从原始返回值生成一个对应Java包装类的实例。这一过程看上去有点像某种装箱和拆箱机制。
当然，其本质仍然是两种不同语言环境之间的类型映射。下面的代码演示一种简单的解决策略：

```kotlin

 import netscape.javascript.JSObject
 
 // 另一个JS对象的包装类
 class FooData(source: JSObject) {
     // ....
     // ....
 }
 
 class JsDate(val source: JSObject) {
    //...这里省略其他代码
     
     fun mappingFooData(data: FooData): JSObject {
         return data.source
     }
     
     fun asFooData(src: Any?): FooData {
         if (src == null || src !is JSObject)
             throw RuntimeException("...")
         return FooData(src)
     }
     
     fun setFooData(data: FooData) {
         source.call("setFooData", mappingFooData(data))
     }
     
     fun getFooData(): FooData {
         val ret = source.call("getFooData")
         return asFooData(ret)
     }
     
 }
```
  除了上面演示的类型映射问题，还存在一些其他的问题，包括undefined值的处理的问题、JS对象数组的实现问题等等。
这些问题的解决可能并不困难，真正难点在于规模，或者说在于代码规模膨胀所引发的大量低效重复性劳动以及基础代码难以维护的问题。
因此，我们需要的是能够帮助我们完成大多数重复性工作、使我们的重复性劳动降到最低的工具。 而这恰恰使本项目的出发点和目标。

  为了实现这一点，本项目使用KSP库实现了编译时注解的扫描和处理。它能够根据接口中定义的函数和属性以及注解中定义的参数，
自动生成对应JS对象的包装类代码。同时，针对前面提到的类型映射、undefined值的解释、JS对象数组等问题，本项目也做了相应的一些处理。

## 本项目包含的模块

### js-object-wrapper-core模块

  这个模块包含JsObjectWrapper核心接口以及JS对象数组的基本实现（JsWrapperObjectArray抽象类）。
所有的JS对象包装类，都必须实现JSObjectWrapper接口。


### js-object-wrapper-annotation模块

  包含了生成JS对象包装类代码所需的注解，同时定义了类型映射机制所需要的一部分代码。
  这些注解包括：

- @JsObjectClass
- @JsObjectFunction
- @JsObjectFunction
- @JsObjectParameter

### js-object-wrapper-compile模块

  实现了上述注解的处理器。通过解析上述注解，在编译时生成代码。

### js-object-wrapper-sample模块

  包含了一些使用范例。

## 开始使用

### 1、添加jitpack仓库，并导入依赖

```kotlin
// Kotlin DSL，groovy同理
repositories {
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    
}
```

### 2、在JS对象所对应的Kotlin接口上添加注解
  假设，你在JS中有如下两个对象（类）需要映射到Java中。注意，这里为了突出对象的属性和接口，省略了对象的具体实现代码（具体实现在js-object-wrapper-sample模块中可以找到）。
```javascript
// 需要映射的对象：Point
class Point {
    x = 0
    y = 0

    constructor(x, y) {/**省略**/}

    isOrigin() {/**省略**/}

    plus(point) {/**省略**/}
	
	move(x, y) {/**省略**/}
	
	toString() {/**省略**/}
}

// 需要映射的对象：Line
class Line {
    start = new Point(0, 0)
    end = new Point(0, 0)

    constructor(start, end) {/**省略**/}

    length() {/**省略**/}

    contains(point) {/**省略**/}

    toString() {/**省略**/}
}
```

  接着，根据JS对象中的函数和属性编写kotlin接口。在kotlin接口中，可以只声明你需要的那些属性和函数。需要注意的是，接口必须实现JsObjectWrapper接口。接口名称是任意的，可以与JS中的相同，也可以有所区别。然后，为接口添加@JSObjectClass注解。

```kotlin
// 映射JS Point对象
@JsObjectClass
interface JsPoint: JsObjectWrapper {
    var x: Double
    var y: Double
    fun isOrigin(): Double
    fun plus(other: JsPoint): JsPoint
    fun move(x: Double, y: Double): JsPoint
    override fun toString(): String
}

// 隐射JS Line对象
@JsObjectClass
interface JsLine: JsObjectWrapper {
    val start: JsPoint
    val end: JsPoint
    fun length(): Double
    fun contains(point: JsPoint): Boolean
    override fun toString(): String
}
```


### 3、构建项目，生成代码

  需要构建一次项目才能生成代码。按照默认的设置，生成的文件名为**Abs{interface_name}.kt**，该文件中包含一个同名的抽象类，即**abstract class Abs{interface_name}**。该抽象类中包含自动生成的代码，以及一些需要在子类中实现的模板函数（抽象函数）。

  例如，在上面的例子中，构建成功之后会生成**abstract class AbsJsPoint**、**abstract class AbsJsPoint** 。以AbsJsPoint为例，其内容大致如下：

```kotlin
public abstract class AbsJsPoint(public override val source: JSObject) : JsPoint {
  public override var x: Double
    get() {

      val ret = source.getMember("x")

      if (ret !is kotlin.Double) throw RuntimeException("the type of return value is not as expected.")
      return ret
    }
    set(v) {
      source.setMember("x", v)
    }

  public override var y: Double
    get() { /**省略**/ }
    set(v) { /**省略**/ }

  public open fun __mappingJsObjectWrapper__(arg: JsObjectWrapper?): JSObject? = arg?.source
    
  public abstract fun asJsPoint(src: Any?): JsPoint

  public override fun isOrigin(): Double {  /**省略**/  }

  public override fun plus(other: JsPoint): JsPoint {
    val ret = source.call("plus", __mappingJsObjectWrapper__(other))
    return asJsPoint(ret)
  }

  public override fun move(x: Double, y: Double): JsPoint {
    val ret = source.call("move", x, y)
    return asJsPoint(ret)
  }

  public override fun toString(): String { /**省略**/ }

  public companion object
}
```

  需要注意的是，可能需要将生成文件的路径添加到源码集中，否则IDE可能无法感知到这些文件的存在。
一般而言，自动生成的文件位于以下位置：

```
build/generated/ksp/main/kotlin/
build/generated/ksp/main/java/
build/generated/ksp/main/resources/
```

例如，在build.gradle.kts中
```kotlin
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
```

### 4、继承自动生成的抽象类，实现相关模板方法，或者覆盖默认逻辑

  从自动生成的**AbsJsPoint**类可以看出，从接口自动生成的是一个抽象类，因此它是无法直接实例化的。至于为什么生成的是抽象类。从**AbsJsPoint**的代码中不难看出，生成的类中包含一些抽象方法，这些抽象方法实际上是类型映射机制产物。当接口中的属性、函数返回值、函数参数的类型超出WebEngine自动类型映射的范畴时，就需要我们手动的进行类型映射，例如上面的plus函数，该函数接受一个JsPoint类型的参数，并且返回一个JsPoint的参数：

```kotlin
  public override fun plus(other: JsPoint): JsPoint {
    val ret = source.call("plus", __mappingJsObjectWrapper__(other))
    return asJsPoint(ret)
  }
```

  在这种情况下，类型映射包含两个层面：第一，在调用底层JSObject对象的call函数前，需要输入参数转换为正确的类型；第二，在函数返回前，将原始的返回值构建成正确的类型。而这也是本项目的类型映射机制的核心所在。对于第一点，如果输入参数的类型为JsObjectWrapper及其子类或者是JsArrayInterface及其子类的话，那么系统会自动生成一个转换函数（例如本例中的`__mappingJsObjectWrapper__`）这些函数并非抽象函数，因为系统知道怎么处理JsObjectWrapper或JsArrayInterface对象；然而，如果输入参数并非JsObjectWrapper、JsArrayInterface，那么系统知道的仅仅是，这些参数在传入JS前需要进行转换，而具体如何进行转换，系统则无法进行推断，需要我们自己加以明确，因此在这种情况下，系统会生成一个抽象函数，告诉我们，需要在子类中实现这个函数，以完成具体了类型映射逻辑。对于第二点，道理是类似的，如果函数所需要的返回值无法通过WebEngine的自动类型映射得到，它就会将这种映射逻辑推迟到子类中，这种设计是合理的，例如，在上面的例子中，plus函数需要一个JsPoint类型的对象作为返回值，而JsPoint本身是一个接口，因此，系统怎么能知道如何创建一个JsPoint对象呢？所以，系统生成了一个asJsPoint()抽象方法，让我们自己在子类中实现这种逻辑。

  因此，接下来的工作就是创建AbsJsPoint、AbsJsLine的子类，并实现那些需要我们自行决定的类型映射逻辑：

```kotlin
class JsPointImp(source: JSObject): AbsJsPoint(source) {
    override fun asJsPoint(src: Any?): JsPoint {
        if (src !is JSObject) {
            throw RuntimeException("js invoke failed")
        }
        return if (src == this.source)
            this
        else
            JsPointImp(src)
    }
}

class JsLineImp(source: JSObject): AbsJsLine(source) {
    override fun asJsPoint(src: Any?): JsPoint {
        if (src !is JSObject) {
            throw RuntimeException("js invoke failed")
        }
        return JsPointImp(src)
    }
}
```

  当然，需要注意的是，虽然JsPointImp、JsLineImp中都需要映射JsPoint类（都有asJsPoint方法），但在两个类中的实现逻辑是有所不同的。也就是说类型映射的实现应当与具体的使用环境相结合。

  这样，我们就可以在Java（kotlin）中以一种非常自然地方式调用JS对象了：

  1、在index.html中创建相关对象

  ```html
  <!DOCTYPE html>
  <html lang="en">
  <head>
      <meta charset="UTF-8">
      <title></title>
      <script src="Point.js"></script>
      <script src="Line.js"></script>
  </head>
  <body>
  <script>
      let p1 = new Point(0, 0)
      let p2 = new Point(3, 4)
      let line = new Line(p1, p2)
      console.log(p1, p1.toString())
      console.log(p2, p2.toString())
      console.log(line, line.toString())
      console.log(line.length())
      console.log(line.contains({x:0, y:0}))
      console.log(line.contains({x:3, y:4}))
  </script>
  </body>
  </html>
  ```

  2、在kotlin中获取相关JSObject对象，创建对应的包装类对象，使用java的方式调用JS对象接口：

```kotlin
package com.github.zimolab.jow.sample.simpleobject

import com.github.zimolab.jow.sample.simpleobject.jsinterface.JsLine
import com.github.zimolab.jow.sample.simpleobject.jsinterface.JsPoint
import javafx.concurrent.Worker
import netscape.javascript.JSObject
import tornadofx.*

class MainView : View("simple object"){
    private val url = javaClass.getResource("/simpleobject/index.html")!!.toExternalForm()
    override val root = vbox {
        webview {
            engine.loadWorker.stateProperty().addListener { _, _, state ->
                println("当前加载状态：$state")
                if (state == Worker.State.SUCCEEDED) {
                    println("加载成功")
                    // 获取js对象
                    val p1 = engine.executeScript("p1") as JSObject
                    val p2 = engine.executeScript("p2") as JSObject
                    val l = engine.executeScript("line") as JSObject
                    
                    // 创建对应包装类实例
                    val point1: JsPoint = JsPointImp(p1)
                    val point2: JsPoint = JsPointImp(p2)
                    val line: JsLine = JsLineImp(l)
                    
                    // 调用接口
                    println("point1: $point1") // 输出：
                    println("point2: $point2")
                    println("line: $line")
                    println("===========")
                    
                    println("line.length(): ${line.length()}")
                    println("line.contains(point1): ${line.contains(point1)}")
                    println("line.contains(point2): ${line.contains(point2)}")
                    println("line.start: ${line.start}")
                    println("line.end: ${line.end}")
                    println("===========")

                    val point3 = point2.plus(point2)
                    println("point3 = point2.plus(point2): $point3")
                    println("line.contains(point3): ${line.contains(point3)}")
                    println("===========")

                    val point4 = point3.move(1.0, 1.0)
                    println("point4 = point3.move(1.0, 1.0)")
                    println("point3: $point3")
                    println("point4: $point4")
                    println("point4 == point3: ${point4 == point3}")
                    println("line.contains(point3): ${line.contains(point3)}")
                    println("===========")

                    point3.x = -1.0
                    println("point3.x = -1.0: $point3")
                    println("===========")


                }
            }
            engine.load(url)
        }

    }
}
```

  

  **注意：虽然在上面的描述中使用了Java，但实际的编码是使用Kotlin完成的，不过鉴于Kotlin和Java的兼容性和互操作性，而且经过实际测试，在Java中同样可以使用生成的代码。示例如下：**

```java
package com.github.zimolab.jow.sample.simpleobject;

import com.github.zimolab.jow.sample.simpleobject.jsinterface.AbsJsPoint;
import com.github.zimolab.jow.sample.simpleobject.jsinterface.JsPoint;
import netscape.javascript.JSObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaJsPointImp extends AbsJsPoint {
    public JavaJsPointImp(@NotNull JSObject source) {
        super(source);
    }

    @NotNull
    @Override
    public JsPoint asJsPoint(@Nullable Object src) {
        if (!(src instanceof JSObject))
            throw new RuntimeException("js invoke failed");
        if (src == this.getSource())
            return this;
        return new JavaJsPointImp((JSObject) src);
    }
}
```

