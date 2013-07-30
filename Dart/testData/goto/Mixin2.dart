abstract class Mixin {
  String mixinMethod() => "mixin";
}

typedef AClass = Object with Mixin;

void main() {
  var a = new AClass();
  print(a.mixin<caret>Method());
}