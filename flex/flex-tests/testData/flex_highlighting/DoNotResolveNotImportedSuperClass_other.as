package foo {
public class Sprite {
    public function foo():void {}
}
}
package bar {
import flash.display.Sprite;

public class SuperClass extends SuperSuperClass {}

public class SuperSuperClass extends Sprite {
    public static var someVar;
}
}