package {

public class NonVoidMethod4 {

    [Test]
    public function <warning descr="Test method should return void">foo</warning>(): uint {
    if (1<2) {
        return 0;
    } else {
        <error>return;</error>
    }
    }
}
}