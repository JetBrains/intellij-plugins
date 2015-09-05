package {
import foo.Sprite;
import bar.SuperClass;

public class DoNotResolveNotImportedSuperClass extends SuperClass {
    public function DoNotResolveNewExprToSuperClass() {
        var s:Sprite = new Sprite();
        new Sprite().foo();
        <error descr="Unresolved variable or type SuperSuperClass">SuperSuperClass</error>;
        <error descr="Unresolved variable or type SuperSuperClass">SuperSuperClass</error>.someVar;
    }
}
}