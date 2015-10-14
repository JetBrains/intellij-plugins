package  {

public class GenerateToString_3 extends SuperClass {

    public override function toString():String {
        return super.toString();
    }
}
}

class SuperClass extends SuperSuperClass {
    [Bindable]
    public var bindableField:String;
}

class SuperSuperClass {
    public function toString():String {
        return super.toString();
    }
}
