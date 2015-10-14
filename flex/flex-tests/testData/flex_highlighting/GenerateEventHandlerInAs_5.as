package {
public class GenerateEventHandlerInAs_5 {
    private function foo():void{
        <error>aaa</error>.bbb().addEventListener('myEvent',<error> </error><caret>);
    }
}
}