interface <lineMarker descr="Has implementations">Int</lineMarker> {
    function foo():void;
}
class <error descr="Method foo from interface Int is not implemented">Impl</error> implements Int {
  public static function f<caret>oo():void {
  }
}
