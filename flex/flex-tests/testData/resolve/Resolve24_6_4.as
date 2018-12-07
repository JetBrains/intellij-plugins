package {
    public class ResolveTest3 {

        private var s:String;
        private var i:int;

        public function ResolveTest3(s:String, i:int):void {
            this.s = s;
            this.i = i;
        }

        public static function xxx() {
          var test:ResolveTest3 = new Resolve<caret>Test3("s", 1);
        }
    }
}