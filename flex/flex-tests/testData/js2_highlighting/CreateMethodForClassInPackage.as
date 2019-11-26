package <error descr="Package name 'foo' does not correspond to file path ''">foo</error> {
public class CreateMethodForClassInPackage {

    public static function getSomeClass():CreateMethodForClassInPackage {
        return new CreateMethodForClassInPackage();
    }

    public function foo():void {
        var obj:CreateMethodForClassInPackage = getSomeClass();
        getSomeClass().<error descr="Unresolved function or method baz()">b<caret>az</error>();
    }
}
}
