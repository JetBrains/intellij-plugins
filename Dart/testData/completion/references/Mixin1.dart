abstract class Mixin {
  String bar() => "bar mix";
  String baz() => "baz mix";
}

class AClass extends Object with Mixin {
}

void main() {
  var a = new AClass();
  print(a.b<caret>);
}