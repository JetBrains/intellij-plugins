package {
public class Sub extends Super {

    private var _opacity:Number;
    public function get opacity():Number {
        return _opacity;
    }

    public function set opacity(value:Number):void {
        _opacity = value;
    }
}
}