abstract class Person {
  final _name;
  Person(this._name);
  String greet(who) => <selection>'Hello, $who. I am $_name.'</selection>;
  void updateChildren();
}