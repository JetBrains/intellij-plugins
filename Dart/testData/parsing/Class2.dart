class Baz extends Kuk implements A, B, C {
  static final y = 87, z = 42;
  static final Foo moms = 42, kuks = 42;
  final Kuk hest;
  
  /* Try a few
   * syntactic constructs. */
  static var foo;
  var fisk = 2;
  final fiskHest = const Foo();

  /* Try a few
   * syntactic constructs. */
  void baz() {
    if (42) if (42) 42; else throw 42;
    switch (42) { case 42: return 42; default: break; }
    try { } catch (var e) {
     rethrow
     rethrow;
    }
    int kongy(x,y) { return 42; }  // This is a comment.
    for (var i in e) {}
    for (var i in e.baz) {}
    for (var i in e[0]) {}
    for (final i in e) {}
    for (final Foo<int> i in e) {}
    for (Foo<int> i in e) {}
    for (i in e) {}
  }

  int hest(a) {
    for (var i = 0; i < a.length; i++) {
      a.b.c.f().g[i] += foo(i);
      int kuk = 42;
      (kuk);
      id(x) { return x; }
      int id(x) { return x; }
      var f = () { };
      var f = int;
      horse() { };
      assert(x == 12);
    }
  }

  Baz(x, y, z) : super(x, y, z) {}

  static final int operatorOrderMap = (){
    return new Whatever()(3, param);
  }();
}