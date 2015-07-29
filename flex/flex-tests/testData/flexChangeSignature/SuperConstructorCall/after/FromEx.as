package {
 class FromEx extends From {
     public function FromEx(p:String) {
         super(p, true);
         trace("abc");
         if (true){
             zzz();
         }
     }
 }
}
