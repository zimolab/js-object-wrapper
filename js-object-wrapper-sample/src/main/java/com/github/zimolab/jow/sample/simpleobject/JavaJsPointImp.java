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
