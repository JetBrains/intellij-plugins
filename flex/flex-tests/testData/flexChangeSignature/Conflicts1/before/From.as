package {
 class From {
     public function foo(p1:Stri<caret>ng, p2:String) {
         if (a < c) {
             var p2;
         }
     }

     function bar() {}

     function pp() {  // propagate
         foo("", "");

         var p1, p2, p3;
     }
 }
}


package {
    class FromEx extends From {
        override public function foo(p1:String, p2:String) {
         var p3;
     }
    }

    function pp2() {  // propagate
         foo("", "");

         var p1;
     }
}

package com {
    function zz() {
        new FromEx().foo();
    }
}