package {

public class NonVoidMethod1 {

    [Test]
    public function <warning descr="Test method should return void">foo</warning>():Number {

    if (1<2) {
        <error>return</error>
    } else {
        <error>return;</error>
    }
        function zz():String {
            return "";
        }
    }
}
}