var a,
b;

topLevel()<fold text='{...}' expand='false'>{
}</fold>

get topGetter1 => 1;

get topGetter2 <fold text='{...}' expand='false'>{
  return 2;
}</fold>

set topSetter(p) <fold text='{...}' expand='false'>{
}</fold>

class A <fold text='{...}' expand='true'>{
  A()
  <fold text='{...}' expand='false'>{
  }</fold>

  A.named()<fold text='{...}' expand='false'>{ }</fold>

  factory A.factory() <fold text='{...}' expand='false'>{
  }</fold>
}</fold>

abstract class B <fold text='{...}' expand='true'>{
  fun1(x)   <fold text='{...}' expand='false'>{
  }</fold>

  fun2();
  fun3() => 1;

  get getter1 => 1;

  get getter2 <fold text='{...}' expand='false'>{
    return 2;
  }</fold>

  set setter(p)<fold text='{...}' expand='false'>{
  }</fold>

  operator ==(z) <fold text='{...}' expand='false'>{
    if (true)<fold text='{...}' expand='true'>{
      // a
    }</fold>
  }</fold>
}</fold>
