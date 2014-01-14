class Object {
  var x;
  int foo() {
    bar().baz();
    return 42;
  }
  bar(int x, int y, z) { }
}

class X = Y;
class X = Y implements Z;
abstract class ListBase<E> = Object with ListMixin<E>;
class Z = foo.bar.Baz<a.b.c<d.e.f, g.h.i>, j> with Y, X, foo.bar.Baz2<a.b.c<d.e.f, g.h.i>, j> implements Q, foo.bar.Baz2<a.b.c<d.e.f, g.h.i>, j>, W;
