package rawTests {
import net.digitalprimates.fluint.tests.TestCase;

public class FlunitTest extends TestCase {
    [Test]
    public function foo():void {
        var x:int = 2 + 2;
        assertEquals(4, x);
    }
}
}