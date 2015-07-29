package {
        use namespace mynamespace;
        public class OverrideCustomNsFromSwc extends ClassA {
            mynamespace override function foo():void {<caret>
            }

        }
}