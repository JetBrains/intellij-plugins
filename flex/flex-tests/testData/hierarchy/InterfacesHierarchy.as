package pack{

public interface Interface1{}
public interface Interface2 extends Interface1{}
public interface Interface3 extends Interface2{}
public interface Interface4 extends Interface2, Interface3{}
public interface Interface5 extends Interface3{}
public class Interface4Impl extends InterfacesHierarchy implements Interface4{}
public class Interface3Impl implements Interface3{}
}