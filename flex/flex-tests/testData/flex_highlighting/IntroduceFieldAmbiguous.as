package {

public class IntroduceFieldAmbiguous {

    import com.bar.A;
    import com.foo.A;

    public function foo(): com.bar.A {
        var v : com.foo.A;
        return null;
    }

    public function test() {
        <error descr="Unresolved variable or type v">v</error> = foo();
    }


}
}
