package {
import com.foo.ClassInComFoo;
import com.foo.SubClassInComFoo;
import com.foo.functionInComFoo;
import com.foo.variableInComFoo;

public class Test {
    public function Test() {
        var topLevelClass : TopLe<caret expected="TopLevelClass">velClass = new TopLev<caret expected="TopLevelClassConstructor">elClass("foo");
        topLevelClass.pro<caret expected="TopLevelClassProperty">perty;
        topLevelClass.readonlyP<caret expected="TopLevelClassReadonlyProperty">roperty;
        topLevelClass.writableP<caret expected="TopLevelClassWritableProperty">roperty = 1;
        topLevelClass.met<caret expected="TopLevelClassMethod">hod("foo", 1);

        var v4 : TopLev<caret expected="TopLevelSubClass">elSubClass;

        var classInComFoo : Clas<caret expected="ClassInPackage">sInComFoo = new ClassIn<caret expected="ClassInPackageConstructor">ComFoo("bar");
        classInComFoo.pro<caret expected="ClassInPackageProperty">perty;
        classInComFoo.readonlyP<caret expected="ClassInPackageReadonlyProperty">roperty;
        classInComFoo.writableP<caret expected="ClassInPackageWritableProperty">roperty = 1;
        classInComFoo.met<caret expected="ClassInPackageMethod">hod("foo", 1);

        var v3 : SubClas<caret expected="SubClassInPackage">sInComFoo;

        topLev<caret expected="TopLevelFunction">elFunction(0, null);
        var v5 = topLev<caret expected="TopLevelVariable">elVariable;

        function<caret expected="FunctionInPackage">InComFoo(0, null);
        var v6 = variableIn<caret expected="VariableInPackage">ComFoo;

    }
}
}