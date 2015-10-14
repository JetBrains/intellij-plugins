package com.intellij.flex.uiDesigner.flex.states {
import mx.core.ITransientDeferredInstance;
import mx.core.UIComponent;
import mx.states.SetProperty;

public class SetStyle extends mx.states.SetStyle {
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
