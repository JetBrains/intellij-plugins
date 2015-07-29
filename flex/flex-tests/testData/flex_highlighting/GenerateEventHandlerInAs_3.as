package {
import mx.controls.Button;

public class GenerateEventHandlerInAs_3 extends Button {
    private function foo():void{
        addEventListener('myEvent',<caret><error>)</error>;
    }
}
}