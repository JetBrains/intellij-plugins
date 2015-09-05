package {

import <error>flexunit</error>.framework.TestCase;

class Methods1NoFlexUnit extends <error>TestCase</error> {

    
    function testNonPublic() {}

    public static function testStatic():void {}

    public function testWithParam(p:String) {}

    public function testReturnType() : Number { return 0; }

    public function get testGet() : Number { return 1; }

    public function set testSet(p :Number) {}

    [Test]
    public function testWithAnnotation() {}

}
}