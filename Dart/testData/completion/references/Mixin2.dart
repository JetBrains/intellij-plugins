abstract class Mixin {
  String bar() => "bar mix";
  String baz() => "baz mix";
}

typedef AClass = Object with Mixin;

void main() {
  var a = new AClass();
  print(a.b<caret>);
}