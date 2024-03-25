mixin mixin(){}
mixin mixin;
mixin M {}
@a @b() @c<String>() @d<List<String>> mixin M1<T, Y extends Comparable<Y>> on I<T>, J implements J,I<T> {}

class A<A>= Object with M;
class A<A extends B<E>>= Object with M;

class Object {
  var x;
  int foo() {
    a >= 1;
    a >>= 1;
    a >> 1;
    a >>>= 1;
    a >>> 1;
    bar().baz();
    return 42;
  }
  bar(int x, int y, z) { }
}

class X = Y;
class X = Y implements Z;
abstract class ListBase<E> = Object with ListMixin<E>;
class Z = foo.bar.Baz<a.b.c<d.e.f, g.h.i>, j> with Y, X, foo.bar.Baz2<a.b.c<d.e.f, g.h.i>, j> implements Q, foo.bar.Baz2<a.b.c<d.e.f, g.h.i>, j>, W;

class GettersSetters {
  get x;
  set x(v);
  int get y => a + 1;
  void set y(v) => null;

  static get z {}
  static set z(v) {}
  static int get q { }
  static void set q(v) { }

  external int get e;
  external void set e(v);
  external static int get w;
  external static void set w(v);
  @Foo() @Bar static external static external int get e;
  @Foo() @Bar external static external static void set e(v);
  get get{}   // not valid, but should be parsed
  set set(){} // not valid, but should be parsed
  get get get{}   // not valid, but should be parsed
  set set set(){} // not valid, but should be parsed
}

foo1() sync => 1;
void foo2() sync* => throw a;
int foo3() async => new X();
Future<T<Y>> foo4() async* => y();
foo5() sync {
  sync{}
  bar() sync{}
}
void foo6() sync* {
  bar() sync*{}
  sync*{}
}
int foo7() async {
  async{}
  bar() async{}
}
Future<T<Y>> foo8() async* {
  bar() async*{}
  async*{};
}

foo() {
  for (var i in 2(10)) {
    for (var i in 3[10]) {
      print(i);
    }
  }
}

extension Tricky on int {
 	Iterable<int> call(int to) =>
      Iterable<int>.generate(to - this + 1, (i) => i + this);
}

extension MyFancyList<T> on List<T> {
  int get doubleLength => this.length * 2;
  List<T> operator-() => this.reversed.toList();
  List<List<T>> split(int at) => <List<T>>[this.sublist(0, at), this.sublist(at)];
  List<T> mapToList<R>(R Function(T) convert) {
    this.map(convert).toList();
  }
}

extension on on on {}
extension on on {}

extension extension;
extension on;
extension extension() => on;
extension on(){}

abstract final class Vehicle {}
interface class NoExtend {}
interface class MySubclass extends NoExtend {}
@reopen class MySubclass extends NoExtend {}
base class A {}
final class A{}
sealed class A{}
macro class macro{}
augment class augment{}
augment abstract class A{}
abstract class A{}
abstract base class A{}
abstract interface class A{}
abstract final class	 A{}
mixin class A{}
base mixin class A{}
abstract mixin class A{}
abstract base mixin class	A{}
mixin	A{}
base mixin A{}
interface mixin	A{}
final mixin	A{}
sealed mixin A{}

extension type A<T>.named(int it) implements num {}
extension type A.new(int it) implements num, int {}
extension type const A(int it) implements num, int {}
extension type A<T>.dynamic(int it) {  @a foo()  var a; }

augment class Foo{}
augment mixin Foo{}
augment enum Enum {}
augment extension foo on bar {}
augment extension on bar {}
augment typedef foo = bar;
augment typedef foo(a);

@a augment class Foo{}
@a augment mixin Foo{}
@a augment enum Enum {}
@a augment typedef foo = bar;
@a augment typedef foo(a);
