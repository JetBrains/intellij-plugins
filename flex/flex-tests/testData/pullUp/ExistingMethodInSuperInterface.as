package {
public interface IGrand {
  function foo();
}

public interface ISuper extends IGrand {
}

public class Sub implements ISuper {
  public function foo() {
  }
}

}