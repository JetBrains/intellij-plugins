package pack{

public interface Interface1{}
public interface Interface2 extends Interface3, pack2.Interface4{}
public interface Interface3{}
}

package pack2{
public interface Interface4{}
public class Class2 extends SupertypesHierarchy implements pack.Interface1{}
}