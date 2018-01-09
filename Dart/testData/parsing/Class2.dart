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
    switch (2+2) {
      label0:
      case(a..b()):
        innerLabel: foo;
        label1: label2:
      case 2:
      case 3:
      case a:
        label3: label4:
      default:
    }
    try { } catch (e) {
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
      assert(x = 12 * (2 - 1),);
      assert(a b c);
      assert(a + b, "error");
      assert(a, "error",);
    }
  }

  Baz(x, y, z) : super(x, y, z) {}

  static final int operatorOrderMap = (){
    return new Whatever()(3, param);
  }();
}

fun0() async {
  inner() {
    await a;
  }
  await a;
}
fun1() async {async a;}
fun2() async {yield a;}
fun3() async {sync a;}
fun4() sync* {await a; await for(a in a);}
fun5() sync* {async a;}
fun6() sync* {yield a;}
fun7() sync* {sync a;}
fun8(){
  await a;
  await for(a in a);
  await for(;;);
  var a = b * -await() + c[1];
  b = await[0] + baz() * 6;
}
fun9(){async a; async() await()}
fun10(){yield a; yield()}
fun11(){sync a; sync()}
fun12(){
  yield a;
  yield*a*b;
  inner() async {
    yield a;
    yield*a*b;
    inner2() {
      yield a;
      yield*a*b;
    }
  }
}
