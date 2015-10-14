package pack{
public interface Interface0{}
public interface Interface1 extends Interface0{
    function foo():void;
}
public interface Interface2 extends Interface1 {}
public interface Interface3 extends Interface1 {}
public class Class1 extends MethodHierarchyCommon{}
public class Class2 extends Class1{
    override public function foo():void{}
}
public class Class3 extends Class2{}
}

