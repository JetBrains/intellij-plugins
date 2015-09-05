package {

public class NonVoidMethod3 {

    [Test]
    public function <warning descr="Test method should return void">foo</warning>(): /*foo*/<error>zzz</error> {
    }
}
}