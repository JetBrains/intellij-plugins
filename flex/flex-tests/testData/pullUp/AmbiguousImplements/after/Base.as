package {
import bar.SomeType;

import foo.SomeType;

public class Base implements bar.SomeType {
    private var v : SomeType;

    function ff() {
        var v:bar.SomeType;
    }
}
}