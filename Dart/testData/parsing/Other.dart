#!/bin/dart

class AssignableSyntax {
  test(a) {
    a[0] ? "a" : "b";
    return a[0] ? "a" : "b";
  }
}

class SwitchSyntax {
  void foo() {
    switch (42) {
      case 42:
        var x = 0;
        break;
      case 87:
        throw 42;
        var x = 0;  // Dead code is allowed by the grammar
      default:
        var x = 0;
        return;
        var y = 0;  // Dead code is allowed by the grammar.
    }
  }
}

class Redirection {

  const Redirection() : this.foo();
  const Redirection.bar() : this.foo();

  Redirection() : this.foo();
  Redirection.baz() : this.foo();

}


class FunctionBody {

  // Even constructors can use the => syntax instead of a
  // body. Syntactically okay, but doesn't make much sense since
  // constructors aren't allowed to return anything.
  FunctionBody() : this.x = 99 => 42;

  foo() => 99;
  foo1() => throw new Exception();
  get x => x;
  set y(x) => x + y;  // Setters should be void -- not enforced by syntax.
  operator +(x) => x + 42;

  int foo() => 99;
  int get x => x;
  void set y(x) => x + y;
  int operator +(x) => x + 42;

  bar() {
    baz() => 87;
    int biz() => 87;

    var f = () => 42;

    2.isOdd();
    return () => throw new Exception();
  }
}