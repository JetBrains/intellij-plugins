package {
class FromEx extends From {
    override protected function get v2():Number {
        return _v;
    }

    override protected function set v2(value:Number):void {
        _v = value;
    }
}
}
