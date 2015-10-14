package {
import SourceClass;

[RunWith("SourceClass")]
public class UnchangeableUsages {
    public function test() {
        var v = new SourceClass();
        if (v instanceof SourceClass) {}
    }
}
}