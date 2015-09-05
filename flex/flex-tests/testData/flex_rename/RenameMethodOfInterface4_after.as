package testData.rename {
    interface IBase {
        function fo<caret>o2();
    }

    interface IExtender extends IBase {

    }

    class Impl implements IExtender {
        public function foo2() {}
    }

    class ImplEx extends Impl {
        public override function foo2() {
            if (true) {
                super.foo2();
            }

        }
    }
}