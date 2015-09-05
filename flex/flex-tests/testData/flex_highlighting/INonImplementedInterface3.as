package {
import test.MyClass
import test.MyClass2
public interface INonImplementedInterface3 {
  function foo(param:MyClass2):void;
  function bar():MyClass;
}
}

package foo {
  public class Application {}
}

package test {
  public class MyClass {}
  public class MyClass2 {}
}
