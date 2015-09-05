package {
 class From {
     internal function foo(p:String = "default"):void {
     }

     function zzz() {
         f<caret>oo();
     }
 }
}
