package {
import flash.events.Event;

import mx.controls.Button;

public class GenerateEventHandlerInAs_3 extends Button {
    private function foo():void{
        addEventListener('myEvent', myEventHandler);
    }

    private function myEventHandler(event:Event):void {<caret>
    }
}
}