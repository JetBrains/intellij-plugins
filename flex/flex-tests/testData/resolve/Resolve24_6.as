package {
    public class ResolveTest {

        private var s:String;
        private var i:int;

        public function ResolveTest(s:String, i:int):void {
            this.s = s;
            this.i = i;
        }
    }
}

var test:ResolveTest = new Resolve<caret>Test("s", 1);