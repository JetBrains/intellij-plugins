package {
import flash.events.Event;

public class GenerateEventHandlerInAs_6 {
    public static const FOO="com.foo.strange event #1";

    private function foo():void{
        aaa.addEventListener(GenerateEventHandlerInAs_6.FOO, aaa_strange_event__1Handler);
    }

    private function aaa_strange_event__1Handler(event:Event):void {<caret>
    }
}
}