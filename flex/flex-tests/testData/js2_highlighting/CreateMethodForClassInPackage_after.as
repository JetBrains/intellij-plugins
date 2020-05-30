package foo {
public class CreateMethodForClassInPackage {

    public static function getSomeClass():CreateMethodForClassInPackage {
        return new CreateMethodForClassInPackage();
    }

    public function foo():void {
        var obj:CreateMethodForClassInPackage = getSomeClass();
        getSomeClass().baz();
    }

    private function baz():void {
        <caret>
    }
}
}
