package {
public class DataStepper {

    private var _foo: int;

    public function get f<caret>oo():int {
        return _foo;
    }

    public function set foo(value:int):void {
        _foo = value;
    }

    public function zz() {
        trace('foo');
    }
}
}
