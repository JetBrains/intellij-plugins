package {
import flash.events.MouseEvent;
import flash.display.DisplayObject;

public class GenerateEventHandlerInAs_7 extends DisplayObject {

    private function foo():void{
        addEventListener(MouseEvent.CLICK, clickHandler);
    }

    private function clickHandler(event:MouseEvent):void {<caret>
    }
}
}