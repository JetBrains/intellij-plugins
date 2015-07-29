package pack{
import flash.events.EventDispatcher;

public interface Interface1 {
  function foo():void;
}
public interface Interface2 {
  function foo():void;
}

public class Class1 implements Interface1, Interface2 {

  public function foo():void {
    var a:pack.Class2;
  }

  public function bar(f:Function) {
  }
}

public class Class2 extends Class1 {

  public function Class2() {
    bar(null);
  }

  override public function foo():void {
    bar(null);
    super.bar(null);
  }

  override public function bar(f:Function) {
    return super.bar(foo);
  }

  public static function f():void{
    var a:EventDispatcher;
    a.addEventListener("someEvent", function(){ global()});
}

}
}

import flash.events.EventDispatcher;
import pack.Class1;
import pack.Class2;
import pack.Interface1;
import pack.Interface2;

public function global(...aa) {
  var a:Interface1;
  var a2:Interface2;
  var b:Class1;
  var c:pack.Class2;
  a.foo();
  a2.foo();
  b.bar(null);
  c.bar(null);
  global(a)
}

public function global2():void{
    var a:EventDispatcher;
    a.addEventListener("someEvent", function(){ global()});
}
