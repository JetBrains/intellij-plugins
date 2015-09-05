package {
import com.IBar;

public class Usage {
    public function pp(p:IBar):IBar {
        var v : IBar = new From();

        var v2: IBar;
        v2.foo();

        var v3: From;
        v3.foo2();
    }
}
}