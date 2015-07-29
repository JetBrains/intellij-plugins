package {
 class From {
     private function foo(/*myparam*/p:Stri<caret>ng = "abc"            ):String {
      trace(p);
    }

     private function zzz() {
         foo("123");
     }
 }
}
