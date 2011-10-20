package com.intellij.flex.uiDesigner.flex.states {
import com.intellij.flex.uiDesigner.flex.DeferredInstanceFromBytesContext;

import mx.core.mx_internal;
import mx.states.State;

use namespace mx_internal;

public final class State extends mx.states.State {
  public var context:DeferredInstanceFromBytesContext;
  
  mx_internal override function dispatchEnterState():void {
    // null if no ARRAY_OF_DEFERRED_INSTANCE_FROM_BYTES in this state
    if (context != null) {
      context.reader.createDeferredMxContainersChildren(context.readerContext.moduleContext.applicationDomain);
    }
    
    super.dispatchEnterState();
  }
}
}
