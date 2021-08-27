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
                    val point1: JsPoint = JsPointImp(p1)
                    val point2: JsPoint = JsPointImp(p2)
                    val line: JsLine = JsLineImp(l)

                    println("point1: $point1")
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

                    JavaTest.test(p1)


                }
            }
            engine.load(url)
        }

    }
}