package com.app.util;

import org.apache.tapestry5.annotations.Component;

@Deprecated
@SuppressWarnings("warning1")
public class Class1 extends Class4 {

    @Component(parameters = {"start=5", "end=1", "value=countValue"})
    private int field1;

    /**
     * field2.
     * docs.
     */
    @Deprecated
    public Class1 field2;

    public int[] field3;

    /**
     * method1 doc.
     */
    @Deprecated
    @SuppressWarnings("")
    public Class1 method1(Class1 param1, int param2, int[] param3) {
        return null;
    }

    public int[] method2() {
        return null;
    }

    public int method3() {
        return 0;
    }

    private void method4() {
    }

    private Whatever method5() {
    }
}
