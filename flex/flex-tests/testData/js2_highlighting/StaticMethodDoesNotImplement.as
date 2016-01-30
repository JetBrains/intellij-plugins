interface <lineMarker descr="Has implementations"><info>Int</info></lineMarker> {
    function <info descr="instance method">foo</info>():void;
}
class <error>Impl</error> implements <info>Int</info> {
  public static function <info descr="static method">f<caret>oo</info>():void {
  }
}
