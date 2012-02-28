package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.ClassPool;

import org.jetbrains.util.ActionCallback;
import org.jetbrains.util.ActionCallbackRef;

public final class FlexLibrarySet extends LibrarySet {
  public static const SWF_POOL:String = "_s";
  public static const IMAGE_POOL:String = "_b";
  public static const VIEW_POOL:String = "_v";

  private var _s:ClassPool;
  private var _b:ClassPool;
  private var _v:ClassPool;

  private var actualFillCalback:ActionCallback;

  private var _currentFillCallbackRef:ActionCallbackRef;
  public function get currentFillCallbackRef():ActionCallbackRef {
    if (_currentFillCallbackRef == null) {
      _currentFillCallbackRef = new ActionCallbackRef();
      // actualFillCalback is actual only
      // after first call createFillCallback()
      // (if it is first call and actualFillCalback is null) and
      // before any new fill request (fill request will call this method, currentFillCallbackRef getter)
      // or if fill action was processed (see nullifyActualFillCalback)
      // we assume, that our socket data processor is synchronous (actually, it is)
      actualFillCalback = null;
    }
    return _currentFillCallbackRef;
  }

  public function FlexLibrarySet(id:int, parent:LibrarySet) {
    super(id, parent);
  }

  public function getClassPool(id:String, createIfNeed:Boolean = true):ClassPool {
    var pool:ClassPool = this[id];
    return createIfNeed && pool == null ? (this[id] = new ClassPool(id, this)) : pool;
  }

  public function createFillCallback():ActionCallback {
    if (actualFillCalback != null) {
      return actualFillCalback;
    }

    if (_currentFillCallbackRef == null) {
      return null;
    }

    var fillCallbackRef:ActionCallbackRef = _currentFillCallbackRef;
    _currentFillCallbackRef = null;

    actualFillCalback = new ActionCallback(fillCallbackRef.usageCount);
    fillCallbackRef.value = actualFillCalback;

    actualFillCalback.doWhenProcessed(nullifyActualFillCalback);
    return actualFillCalback;
  }

  private function nullifyActualFillCalback():void {
    actualFillCalback = null;
  }
}
}
