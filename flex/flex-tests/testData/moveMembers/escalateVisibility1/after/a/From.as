package a {
import b.To;

public class From {


    public function From() {
        To.privateMethod();
        To.internalMethod1();
        To.internalMethod2();
        To.publicMethod();
        To.protectedMethod();

        To.privateField = 0;
        To.internalField1 = 0;
        To.internalField2 = 0;
        To.publicField = 0;
        To.protectedField = 0;
    }
}
}