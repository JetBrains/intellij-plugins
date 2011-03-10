package com.intellij.flex.uiDesigner.flex.states {
import mx.core.ITransientDeferredInstance;
import mx.core.UIComponent;
import mx.states.AddItems;

public final class AddItems extends mx.states.AddItems {
  override protected function getOverrideContext(target:Object, parent:UIComponent):Object {
    if (target is ITransientDeferredInstance) {
      return ITransientDeferredInstance(target).getInstance();
    }
    else {
      return super.getOverrideContext(target, parent);
    }
  }
}
}