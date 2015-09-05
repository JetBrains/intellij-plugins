package testData.rename {
    interface IBase {
        function fo<caret>o();
    }

    interface IExtender extends IBase {

    }

    class Impl implements IExtender {
        public function foo() {}
    }

    class ImplEx extends Impl {
        public override function foo() {
            if (true) {
                super.foo();
            }

        }
    }
}