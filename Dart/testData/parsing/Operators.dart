// Test shift operators.
class Shifting {
  operator >>(other) {
    Box<Box<prefix.Fisk>> foo = null;
    return other >> 1;
  }
}

var x = a ? b : c ? d : e ? f : g;

class Operators {

  @Object() external void operator ~(); // void is id

  @Object() operator operator *(x) => foo(bar(baz)) + 1; // operator is id, not valid, but should be parsed
  @Object() external operator /(x);
  external operator operator %(x);
  @Object() operator ~/(x) {}

  external operator +(x);
  operator operator -(x);

  @Foo @Bar static operator <<(x) {} // static is id, not valid, but should be parsed
  void operator >>(x) {}  // void is id

  part operator >=(x) {} // part is id
  show operator >(x) {} // show is id
  Foo operator <=(x) {}
  external external operator <(x); // 2nd external is id, not valid, but should be parsed

  operator ==(x) {}

  operator &(x) {}
  operator ^(x) {}
  operator |(x) {}

  operator [](x) {}
  operator []=(x,y) {}

  foo() {
    ~super;
    -super;

    super * 42;
    super / 42;
    super % 42;
    super ~/ 42;

    super + 42;
    super - 42;

    super << 42;
    super >> 42;

    super == 42;
    super != 42;  // Expected to map to !(super == 42).
    super <= 42;
    super < 42;
    super >= 42;
    super > 42;

    super & 42;
    super ^ 42;
    super | 42;

    // BUG(4994724): Do we need to allow calling these?
    !super;
  }
}