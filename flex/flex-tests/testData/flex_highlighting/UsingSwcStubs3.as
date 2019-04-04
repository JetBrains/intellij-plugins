package {
import somePackage.AClassInPackage;
import somePackage.namespaceInPackage1;
<warning>import somePackage.namespaceInPackage2;</warning>

public class UsingSwcStubs3 {
    aNamespace var p;
    var d = aVar;
    var v2 = aConst;

    var d2 = AClass.constInClassStatic;
    var d3 = AClass.varInClassStatic;
    var d4 = new AClass().constInClass;
    var d5 = new AClass().varInClass;

// FIXME the following line is an error
    AClass.NamespaceInClass var d6;

    namespaceInPackage1 var t;
    <warning descr="Qualified name may be replaced with import statement">somePackage.namespaceInPackage2</warning> var v;
    <error>namespaceInPackage3</error> var t2;

    var d7 = AClassInPackage.constInClassInPackageStatic;
    var d8 = AClassInPackage.varInClassInPackageStatic;

}
}

class Base {
    public static function staticMethodName():void {
        trace("Base.staticMethodName");
    }
}