package mypackage {
  class MyClass {
    [Inspectable(category="General")]
    public native function set height(_:uint):void;

    [Bindable]
    public function get employee(): Object {
      return null;
    }
    public function set employee(x:Object):void {
    }
  }
}

class mypackage.MyClass3 extends mypackage.MyClass {}

public interface foo.baz.IComponent {}