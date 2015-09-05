package {

public class IntroduceFieldAmbiguous {

    import com.bar.A;
    import com.foo.A;

    private var v:com.bar.A;

    public function foo(): com.bar.A {
        var v : com.foo.A;
        return null;
    }

    public function test() {
        v = foo();
    }


}
}
