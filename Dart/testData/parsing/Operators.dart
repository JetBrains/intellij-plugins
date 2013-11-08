// Test shift operators.
class Shifting {
  operator >>(other) {
    Box<Box<prefix.Fisk>> foo = null;
    return other >> 1;
  }
}

class Operators {

  operator ~() { }

  operator *(x) { }
  operator /(x) { }
  operator %(x) { }
  operator ~/(x) { }

  operator +(x) { }
  operator -(x) { }

  operator <<(x) { }
  operator >>(x) { }

  operator ==(x) { }
  operator <=(x) { }
  operator <(x) { }
  operator >=(x) { }
  operator >(x) { }

  operator &(x) { }
  operator ^(x) { }
  operator |(x) { }
  operator []=(x) { }
  operator [](x) { }

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
    super === 42;
    super !== 42;
  }
}