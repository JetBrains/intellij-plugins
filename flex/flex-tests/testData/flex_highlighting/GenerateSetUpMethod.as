package {
import flexunit.framework.TestCase;

public class GenerateSetUpMethod extends TestCase{
    <caret>
    [After]
    public override function tearDown():void {
        super.tearDown();
    }
}
}