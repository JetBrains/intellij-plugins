package {

import <error descr="Unresolved variable or type flash">flash</error>.display.Sprite;
import <error descr="Unresolved variable or type flash">flash</error>.text.TextField;

public class MissingSdk_2 extends <error descr="Unresolved type Sprite">Sprite</error> {
    public function Untitled22() {
        var textField:<error descr="Unresolved type TextField">TextField</error> = new <error descr="Unresolved type TextField">TextField</error>();
        textField.<error descr="Unresolved variable text">text</error> = "Hello, World";
        <error descr="Unresolved function or method addChild()">addChild</error>(textField);
    }
}
}
