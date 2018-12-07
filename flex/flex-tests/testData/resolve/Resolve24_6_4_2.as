package {
    public class ResolveTest4 {

        private var s:String;
        private var i:int;

        public static function xxx() {
          var test:ResolveTest4 = new Resolve<caret>Test4("s", 1);
        }
    }
}