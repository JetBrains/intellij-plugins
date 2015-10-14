package {
public class Sub extends Base {
  public override function f<caret>oo() {}
}

public class Base extends Super {
  public override function foo() {}
}

public class Super implements IFoo {
  public function foo() {}
}

public interface IFoo {
  function foo();
}

public class Ref {
  function ref() {
    var t : IFoo;
    t.foo();
  }

}
}
