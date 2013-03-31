abstract class Mixin {
  String mixinMethod() => "mixin";
}

class AClass extends Object with Mixin {
}

void main() {
  var a = new AClass();
  print(a.mixin<caret>Method());
}