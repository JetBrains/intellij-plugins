package {
        use namespace mynamespace;
        public class OverrideCustomNsFromSwc extends ClassA {
            mynamespace override function foo():void {
            }

            override mynamespace function bar():void {
                super.mynamespace::bar();
            }
        }
}