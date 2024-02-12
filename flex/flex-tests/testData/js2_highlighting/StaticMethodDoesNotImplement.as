interface <lineMarker descr="Has implementations"><info descr="interface">Int</info></lineMarker> {
    function <info descr="instance method">foo</info>():void;
}
class <error descr="Method foo from interface Int is not implemented"><info descr="classes//class name">Impl</info></error> implements <info descr="interface">Int</info> {
  public static function <info descr="static method">f<caret>oo</info>():void {
  }
}
