package skins {
import mx.skins.ProgrammaticSkin;

public class MyNewSkin1 extends ProgrammaticSkin implements IMySkin {
        public function MyNewSkin1() {
            super();
        }

    override public function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
    }
}
}