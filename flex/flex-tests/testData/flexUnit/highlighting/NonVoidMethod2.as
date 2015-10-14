package {

public class NonVoidMethod2 {

    [Test]
    public function <warning descr="Test method should return void">foo</warning>() :int {
    }
}
}