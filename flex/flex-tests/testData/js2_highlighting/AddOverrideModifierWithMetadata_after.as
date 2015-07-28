package {
    public class AddOverrideModifierWithMetadata{

        public function foo():void {}

    }
}

class B extends AddOverrideModifierWithMetadata {

    [Before]
    override public function foo():void {}
}