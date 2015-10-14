package {

import com.sample.<error descr="Unresolved variable lib">lib</error>.LibraryClassInPackage;
import com.sample.lib2.Library2ClassInPackage;
import com.sample.<error descr="Unresolved variable lib3">lib3</error>.Library3ClassInPackage;
import com.sample.lib4.Library4ClassInPackage;

public class TestScope_1 {
    var v1 : <error descr="Unresolved type LibraryClass">LibraryClass</error>;
    var v2 : <error descr="Unresolved type LibraryClassInPackage">LibraryClassInPackage</error>;

    var v3 : Library2Class;
    var v4 : Library2ClassInPackage;

    var v5 : <error descr="Unresolved type Library3Class">Library3Class</error>;
    var v6 : <error descr="Unresolved type Library3ClassInPackage">Library3ClassInPackage</error>;

    var v7 : Library4Class;
    var v8 : Library4ClassInPackage;

    var m2: <error>M2</error>;
    var m3: <error>M3</error>;
}
}
