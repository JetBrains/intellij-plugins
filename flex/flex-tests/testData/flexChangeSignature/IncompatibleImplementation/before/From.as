package {
 interface A {
   function fo<caret>o(i: int)
 }
}

package {
 class B implements A {
   public function foo() {}
 }
}

package {
 class C implements A {
   public function foo(i: int) {}
 }
}
