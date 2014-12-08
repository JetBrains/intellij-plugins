var a,
b;

topLevel()<fold text='{...}' expand='true'>{
}</fold>

get topGetter1 => 1;

get topGetter2 <fold text='{...}' expand='true'>{
  return 2;
}</fold>

set topSetter(p) <fold text='{...}' expand='true'>{
}</fold>

class A <fold text='{...}' expand='true'>{
  A()
  <fold text='{...}' expand='true'>{
  }</fold>

  A.named()<fold text='{...}' expand='true'>{ }</fold>

  factory A.factory() <fold text='{...}' expand='true'>{
  }</fold>
}</fold>

abstract class B <fold text='{...}' expand='true'>{
  fun1(x)   <fold text='{...}' expand='true'>{
  }</fold>

  fun2();
  fun3() => 1;

  get getter1 => 1;

  get getter2 <fold text='{...}' expand='true'>{
    return 2;
  }</fold>

  set setter(p)<fold text='{...}' expand='true'>{
  }</fold>

  operator ==(z) <fold text='{...}' expand='true'>{
    if (true){
      // a
    }
  }</fold>
}</fold>

enum Foo1{}
enum Foo2{a}
enum Foo3<fold text='{...}' expand='true'>{a }</fold>
enum Foo4<fold text='{...}' expand='true'>{
a,
b,
//c
}</fold>
