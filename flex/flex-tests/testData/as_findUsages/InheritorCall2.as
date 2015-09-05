package {
    class FooBar {
        public function fo<caret>oBar():void {

        }
    }
}
class FooBarImpl extends FooBar {
    public static function main():void {
        new FooBarImpl().fooBar();
    }

    override public function fooBar():void {
    }
}

class FooBarEx extends FooBar {
    public static function main():void {
        new FooBarEx().fooBar();
    }

    override public function fooBar():void {
    }
}
