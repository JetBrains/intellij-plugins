package {
 class From {
     private var _prop;
     private var _prop2;

     public function set p<caret>rop(value):int {
         _prop = value;
         var value2;
     }
 }

}
