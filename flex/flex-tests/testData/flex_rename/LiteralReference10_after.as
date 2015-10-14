package {
public class DataStepper {

    private var _bar: int;

    public function get bar():int {
        return _bar;
    }

    public function set bar(value:int):void {
        _bar = value;
    }

    public function zz() {
        trace('foo');
    }
}
}
