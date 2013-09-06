library lib;

part of Mega.Lib;

export 'a.dart' show A, B hide C, D;
export 'a.dart';

import "QualifiedReturnTypeA.dart" as pref;

part 'test.dart';

class B {
  var get = 1;
  int set = 1;
  final int operator = 1;
}

class G {
  // Old getter syntax
  int get g1() => 1;
  // New getter syntax
  int get g2 => 2;
}

class G {
  int as = 0;
}

class B {
  const factory B.n(int x) = A.m;
}

class B {
  factory B.n(int x) = A.m;
}

class C {
  var f = g..m1()..m2()..f.a;
}

class A {
  var name;
  foo() {}
}

main() {
  A a = new A();
  a..name = true ? 'true' : 'false'
   ..foo();
}

main() {
  (p) {} is String;
}

class A {
  A.named() {}
}

class B {
  factory B() = A.named;
}

class A {

  pref.A foo() {
    return new pref.A();
  }
}

class A { 
  int get;
  var set;
  final operator;
}
class C {
  var get = 1;
  int set = 1;
  final operator = 1;
}
class D {
  int get = 1;
  final int set = 1;
  var operator = 1;
}
class E {
  int get() { }
  void set() { }
  operator() { }
}
class F {
  operator - () { }
  operator + (arg) { }
  operator [] (arg) { }
  operator []= (arg, arg){ }
}

var interface;
bool interface;
final interface;
interface() { }
String interface() { }
interface();
var typedef;
bool typedef;
final typedef;
typedef() { }
String typedef() { }
typedef();
class A { 
  var interface;
  bool interface;
  final interface;
  interface() { }
  String interface() { }
  interface();
  var typedef;
  bool typedef;
  final typedef;
  typedef() { }
  String typedef() { }
  typedef();
}

method() {
  var interface;
  bool interface;
  final interface;
  interface() { }
  String interface() { }
  interface();
  var typedef;
  bool typedef;
  final typedef;
  typedef() { }
  String typedef() { }
  typedef();
}

const annotation = 0;
class A {
  test() {
    @annotation
    var v = 0;
  }
}

class C {
  operator <(v) {}
  operator >(v) {}
  operator <=(v) {}
  operator >=(v) {}
  operator ==(v) {}
  operator -() {}
  operator -(v) {}
  operator +(v) {}
  operator /(v) {}
  operator ~/(v) {}
  operator *(v) {}
  operator %(v) {}
  operator |(v) {}
  operator ^(v) {}
  operator &(v) {}
  operator <<(v) {}
  operator >>(v) {}
  operator [](i) {}
  operator []=(i, v) {}
  operator ~() {}
}

factory CSSMatrix([String cssValue]) {
    if (?cssValue) {
      return _CSSMatrixFactoryProvider.createCSSMatrix();
    }
    return _CSSMatrixFactoryProvider.createCSSMatrix(cssValue);
}

void smth(){
  var items = [1, 2, 3, 4, 5];
  var part = items.filter((i) => i % 2 == 1);
  print(part);
}

abstract class Int8List implements List<int>, ByteArrayViewable {
  external factory Int8List(int length);
  external factory Int8List.view(ByteArray array, [int start, int length]);
}