package {
import com.Foo;
public class CreateSubclassIntention3<caret> {
    public function CreateSubclassIntention3(p:Foo) {
    }
}
}

package <error>com</error> {
public class <error>Foo</error> {
}
}

