package {
[DefaultProperty("children")]
  class DefaultPropertyWithArrayType_2 {
    private var _children:Array

    public function get children():Array {
        return _children;
    }

    [ArrayElementType("mypackage.Label")]
    public function set children(val:Array):void {
        _children = val;
    }
}
}