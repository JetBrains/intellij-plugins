package {
import flash.events.MouseEvent;

import mx.controls.Button;

public class GenerateEventHandlerInAs_2 extends Button {
    private function foo():void{
        var _myButton:Button;
        <caret>_myButton.addEventListener<error>(MouseEvent.DOUBLE_CLICK)</error>;
    }
}
}