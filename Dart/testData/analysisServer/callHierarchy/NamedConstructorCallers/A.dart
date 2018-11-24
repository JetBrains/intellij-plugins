class X {
  int p;
  get it => ((l) => z7(5));
  set it(to) { p = to; }
  static zz(n) => n*n;
  z7(n) => n;
  x(f) {
    f(() => z7(3));
    q() {
      f(() => z7(2));
    }
    q();
  }
  X.a(t) {
    t(() => z7(32));
    q() {
      t(() => z7(23));
    }
    q();
  }
  factory X.r(f) {
    f(() => zz(8));
    q() {
      f(() => zz(4));
    }
    q();
  }
  s() {
    var y = new X.a(() {it = 5;});
    print(y.it);
  }
}