package {

import com.sample.lib.LibraryClassInPackage;
import com.sample.lib2.Library2ClassInPackage;
import com.sample.lib3.Library3ClassInPackage;
import com.sample.lib4.Library4ClassInPackage;

public class TestScope_2 {
    var v1 : LibraryClass;
    var v2 : LibraryClassInPackage;

    var v3 : Library2Class;
    var v4 : Library2ClassInPackage;

    var v5 : Library3Class;
    var v6 : Library3ClassInPackage;

    var v7 : Library4Class;
    var v8 : Library4ClassInPackage;

    var m2: M2;
    var m3: <error>M3</error>;
}
}
