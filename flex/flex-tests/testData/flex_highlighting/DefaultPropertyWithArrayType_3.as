package {
[DefaultProperty("children")]
  class DefaultPropertyWithArrayType_3 {
    private var _children:Object

    public function get children():Object {
        return _children;
    }

    [InstanceType("Array")]
    [ArrayElementType("mypackage.Label")]
    public function set children(val:Object):void {
        _children = val;
    }
}
}