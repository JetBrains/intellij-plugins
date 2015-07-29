package {
import flash.events.MouseEvent;

import mx.controls.Button;

public class GenerateEventHandlerInAs_2 extends Button {
    private function foo():void{
        var _myButton:Button;
        _myButton.addEventListener(MouseEvent.DOUBLE_CLICK, myButton_doubleClickHandler);
    }

    private function myButton_doubleClickHandler(event:MouseEvent):void {<caret>
    }
}
}