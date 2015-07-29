package {
  class F<caret expected="">oo {
      var c1 : C1;
      var c2 : Vector.<C2>;
      var v : Foo;
      function f() {
          var c3 = new C3();
          var c4 : C4;
          var c5 : Vector.<C5>;
      }
  }
}

class C1 { var v : Vector.<C1>; }
class C2 { var v = new C2(); }
class C3 { function foo() { var v : C3; } }
class C4 {}
class C5 {}