package {
use namespace mynamespace;
        public class OverrideCustomNsFromSwc_2 extends ClassA {

            override mynamespace function bar():void {
                super.mynamespace::bar();
            }

            override mynamespace function foo():void {
                super.mynamespace::foo();
            }
        }
}
