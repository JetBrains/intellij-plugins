package {

public class NonPublicMethod1 {

    [Test]
    function <warning descr="Test method should be public">foo</warning>() {
    }
}
}