package {
    public class BazBar {
        function BazBar() {}
        public static function foo():void {}
        public static function bar():void {
            Baz<caret>Bar.foo();
        }
    }
}
BazBar.bar();