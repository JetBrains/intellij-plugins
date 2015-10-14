interface IFoo {
    function f<caret>oo();
}

class Base {
    public function foo() {}
}

class Subclass extends Base implements IFoo {

}