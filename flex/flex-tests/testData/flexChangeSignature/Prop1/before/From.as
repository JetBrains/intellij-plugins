package {
class From {
    protected var _v : String;

    public function get <caret>v():String {
        return _v;
    }

    public function set v(value:String):void {
        _v = value;
    }
}
}
