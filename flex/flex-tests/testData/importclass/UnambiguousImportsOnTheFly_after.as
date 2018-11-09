package {
import baz.UnambiguousField;

import foo.UnambiguousClass;

public class UnambiguousImportsOnTheFly {
    public function UnambiguousImportsOnTheFly():void {
        var a:UnambiguousClass;
        var b:AmbiguosClass;
        var c:UnambiguousField;
    }
}
}