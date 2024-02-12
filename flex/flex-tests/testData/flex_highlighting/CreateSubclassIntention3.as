package {
import com.Foo;
public class CreateSubclassIntention3<caret> {
    public function CreateSubclassIntention3(p:Foo) {
    }
}
}

<error descr="Package should be first statement in file">package</error> <error descr="Package name 'com' does not correspond to file path ''">com</error> {
public class <error descr="Class 'Foo' should be defined in file 'Foo.as'">Foo</error> {
}
}

