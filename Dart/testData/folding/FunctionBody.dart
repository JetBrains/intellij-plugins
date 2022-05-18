var a,
b;

topLevel()<fold text='{...}' expand='true'>{
}</fold>

topLevel2() =><fold text='...' expand='true'>
true;</fold>

/// Async
topLevel3() async <fold text='{...}' expand='true'>{
}</fold>

// Fat arrow, async
topLevel4() async => false;
topLevel4() async =><fold text='...' expand='true'> false ||
    true;</fold>

// Fat arrow, async, no white space
topLevel5()async=><fold text='...' expand='true'>false||
    true;</fold>

/// Getter and Setter
get topGetter1 => 1;

get topGetter2 <fold text='{...}' expand='true'>{
  return 2;
}</fold>

set topSetter(p) <fold text='{...}' expand='true'>{
}</fold>

get topGetter3 =><fold text='...' expand='true'> true ||
    false ||
    true;</fold>

/// Classes
class A <fold text='{...}' expand='true'>{
  A()
  <fold text='{...}' expand='true'>{
  }</fold>

  A.named()<fold text='{...}' expand='true'>{ }</fold>

  factory A.factory() <fold text='{...}' expand='true'>{
  }</fold>

  bool returnBool() =><fold text='...' expand='true'> true ||
    false ||
    true;</fold>

  bool returnBool1() => true || false || true;
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
    if (true)<fold text='{...}' expand='true'>{
      // a
    }</fold>
  }</fold>
}</fold>
