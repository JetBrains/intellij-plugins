class Point1D {
  double _x;
  Point1D(this._x);
  Point1D operator -() => new Point1D(_x);

  foo(){}
}

main(){
  var a = new Point1D(239);
  var b = -a;
  b.fo<caret>o();
}