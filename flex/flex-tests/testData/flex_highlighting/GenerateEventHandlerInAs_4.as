package {
import mx.controls.Button;

public class GenerateEventHandlerInAs_4 extends Button {
    private function foo():void{
        addEventListener('myEvent', <caret><error>handler</error>)
    }
}
}