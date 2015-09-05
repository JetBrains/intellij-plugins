package {
import com.foo.Foo; // this is a class
public class ImportScopeOverriddenByBaseClass extends com.foo.Foo {
    function zzz() {
        import com.bar.Foo; // this is an interface
        var v : Foo; // compiler treats this as a reference to a class, not an interface
        v.classMethod(); // this line compiles ok
        v.<error>interfaceMethod</error>(); // this one gives error: Call to a possibly undefined method interfaceMethod through a reference with static type com.foo:Foo.
    }
}
}