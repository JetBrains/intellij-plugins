package {
    interface IFooBar {
        function fo<caret>oBar():void;
    }
}
class FooBarImpl implements IFooBar {
    public static function main():void {
        new FooBarImpl().fooBar();
    }

    public function fooBar():void {
    }
}
