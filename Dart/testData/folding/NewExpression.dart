library foo;

class A <fold text='{...}' expand='true'>{
  int x, y, z;
  A(this.x, this.y, this.z);
}</fold>

class B <fold text='{...}' expand='true'>{
  B();
}</fold>

f() <fold text='{...}' expand='true'>{
  // this should have the new expression folding
  var a = new A<fold text='(...)' expand='true'>(1, 2, 3)</fold>;

  // this should have the (optional) new expression folding
  var a2 = A<fold text='(...)' expand='true'>(1, 2, 3)</fold>;
  var a3 = a(1, 2, 3);

  // this expression should not as there are no arguments
  var b = new B();
}</fold>