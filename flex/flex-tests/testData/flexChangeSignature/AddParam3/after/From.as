package {
 class From {
     internal function bar(p1:int, p2:String = abc, p3:Boolean = false, ...p4) {}

     function zzz() {
         bar(100, "def");
     }
 }
}
