package {
import test.MyClass
import test.MyClass2

interface INonImplementedInterface {
  function foo(param:MyClass2):void;
  function bar():MyClass;
}
}

package test {
  public class MyClass {}
  public class MyClass2 {}
}
