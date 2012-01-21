package mx.effects {
import mx.core.ITransientDeferredInstance;

public class Effect extends Fffect {
  public function Effect(target:Object = null) {
    super(target);
  }

  override public function get target():Object {
    var target:Object = super.target;
    return (target is ITransientDeferredInstance) ? ITransientDeferredInstance(target).getInstance() : target;
  }

  override public function get targets():Array {
    var t:Array = super.targets;
    for each (var v:Object in t) {
      if (v is ITransientDeferredInstance) {
        var instances:Array = t.slice();
        for (var i:int = 0; i < instances.length; i++) {
          var instance:Object = instances[i];
          if (instance is ITransientDeferredInstance) {
            instances[i] = ITransientDeferredInstance(instance).getInstance();
          }
        }

        return instances;
      }
    }

    return t;
  }
}
}
