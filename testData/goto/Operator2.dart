class Vector {
  double _x;

  double distance() => _x;
  Vector operator +(Vector value) => new Vector(_x + value._x);
}

main() {
  var v = new Vector(239);
  // warning
  print((v + 10).dist<caret>ance());
}