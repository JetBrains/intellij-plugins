package {

[DefaultProperty("buttonArray")]
public class InheritDefaultProperty_other {

    [ArrayElementType("mx.controls.Button")]
    public function set buttonArray(a:Array):void{}
}

public class SubClassWithoutDefaultProperty extends InheritDefaultProperty_other {}
}
