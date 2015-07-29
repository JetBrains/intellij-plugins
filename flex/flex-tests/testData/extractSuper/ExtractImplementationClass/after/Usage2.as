package {
import bar.FromImpl;

import com.From;
import com.IMoved;
import com.INotMoved;

public class Usage2 {
    public function uuu(p:From) {
        trace(p.movedProp);

        var v1: From;
        v1.movedMethod();

        var v2: FromImpl;
        v2.notMovedMethod2();

        var v3: From;
        var v4: IMoved;
        v4 = v3;

        var v5: FromImpl;
        var v6: INotMoved;
        v6 = v5;
    }

    private function trace(p:*) {
        FromImpl.staticFunc();
        trace(FromImpl.staticProp);
        trace(FromImpl.ID);
    }
}
}