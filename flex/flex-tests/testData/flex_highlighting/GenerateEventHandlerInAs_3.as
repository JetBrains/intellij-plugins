package {
import mx.controls.Button;

public class GenerateEventHandlerInAs_3 extends Button {
    private function foo():void{
        addEventListener<error descr="Invalid number of arguments, expected 2..5">('myEvent',<caret><error>)</error></error>;
    }
}
}