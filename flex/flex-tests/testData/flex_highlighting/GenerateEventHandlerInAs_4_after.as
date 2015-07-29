package {
import flash.events.Event;

import mx.controls.Button;

public class GenerateEventHandlerInAs_4 extends Button {
    private function foo():void{
        addEventListener('myEvent', handler);
    }

    private function handler(event:Event):void {<caret>
    }
}
}