class Vector {
  double _x;
  double distance() => _x;
  Vector operator *(double value) => new Vector(value * _d);
}

main(){
  var v = new Vector(239);
  print((v*10).dist<caret>ance());
}