add(Foo foo, {Bar param}) {
  <caret>
}

main() {
  var a = new Foo();
  var result = add(a, param:new Bar());
}

class Foo{}
class Bar{}