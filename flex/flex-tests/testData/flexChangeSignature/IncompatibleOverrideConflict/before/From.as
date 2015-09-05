package {
 class A {
   public function fo<caret>o(i: int) {}
 }
}

package {
 class B extends A {
   override public function foo() {
   }

   function foo2() {}
 }
}


