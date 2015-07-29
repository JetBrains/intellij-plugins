package {
import flash.events.Event;

public class GenerateEventHandlerInAs_5 {
    private function foo():void{
        aaa.bbb().addEventListener('myEvent', myEventHandler);
    }

    private function myEventHandler(event:Event):void {<caret>
    }
}
}