package pack.subpack{
import flash.display.Sprite;
import flash.events.MouseEvent;

import mypackage.Button;
import mx.core.UIComponent;

import pack2.Interface1;

public class Class1 {
  private function getButton():Button {
    return null;
  }

  public function someFunction():void {
    var s:Sprite;
    s = new Button();
    var b:UIComponent;
    b = UIComponent(s);
    if (true) {
      this.getButton().addEventListener(MethodCalleeHierarchy.bar(), myButton_clickHandler);
    }
    global();
    global("a", "b");
    var i:Interface1;
    i.foo();
    Class2(i).foo();
    new Class1();
  }

  private function myButton_clickHandler(event:MouseEvent):void {
  }
}

public class Class2 extends MethodCalleeHierarchy {
  override public function foo():void {
  }
}
}

package pack2 {
import pack.subpack.Class2;

public interface Interface1 {
  function foo():void;
}

public class Class3 extends Class2 {
  override public function foo():void {
    super.foo();
    Class2(null).foo();
  }
}
}
public function global(... x):void {
  global(1);
}
