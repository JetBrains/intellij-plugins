abstract class Person {
  final _name;
  Person(this._name);
  String greet(who) => extracted(who, _name);
  void updateChildren();
}

String extracted(who, _name){
  return 'Hello, $who. I am $_name.';
}