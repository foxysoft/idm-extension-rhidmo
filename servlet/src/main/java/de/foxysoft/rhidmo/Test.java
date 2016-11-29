package de.foxysoft.rhidmo;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;

public class Test {
    public static void main(String[] args) throws Exception {
        Class c = Class.forName("com.sap.idm.extension.TaskProcessingAdapter");
        DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
            .subclass(c)
            .name("de.foxysoft.rhidmo.TaskProcessing")
            .make();
    }
}
