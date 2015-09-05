package {
    public class AddOverrideModifierWithMetadata{

        public function foo():void {}

    }
}

class B extends AddOverrideModifierWithMetadata {

    [Before]
    public function <error>f<caret>oo</error>():void {}
}