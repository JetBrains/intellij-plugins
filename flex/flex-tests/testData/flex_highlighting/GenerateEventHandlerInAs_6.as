package {
public class GenerateEventHandlerInAs_6 {
    public static const FOO="com.foo.strange event #1";

    private function foo():void{
        <error>aaa</error>.addEventListener(GenerateEventHandlerInAs_6.FOO<caret>)
    }
}
}