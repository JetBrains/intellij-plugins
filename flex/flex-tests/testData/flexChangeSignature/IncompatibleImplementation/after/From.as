package {
 interface A {
   function foo2(j:int)
 }
}

package {
 class B implements A {
   public function foo() {}
 }
}

package {
 class C implements A {
   public function foo2(j:int) {}
 }
}
