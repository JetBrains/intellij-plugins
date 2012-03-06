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

  private var documentFactories:Vector.<Object>/* FlexDocumentFactory */;

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

    if (fillCallbackRef.usageCount == 0) {
      // currentFillCallbackRef was requested, but class pool was filled before any request to createFillCallback()
      return null;
    }

    actualFillCalback = new ActionCallback(fillCallbackRef.usageCount);
    fillCallbackRef.value = actualFillCalback;

    actualFillCalback.doWhenProcessed(nullifyActualFillCalback);
    return actualFillCalback;
  }

  private function nullifyActualFillCalback():void {
    actualFillCalback = null;
  }

  public function getDocumentFactory(id:int):Object {
    return documentFactories != null && documentFactories.length > id ? documentFactories[id] : null;
  }

  private var _documentFactoryClass:Class;
  public function get documentFactoryClass():Class {
    if (_documentFactoryClass == null) {
      _documentFactoryClass = Class(applicationDomain.getDefinition("com.intellij.flex.uiDesigner.flex.FlexDocumentFactory"));
    }

    return _documentFactoryClass;
  }

  public function documentUnregistered(id:int):void {
    if (documentFactories != null && id < documentFactories.length) {
      documentFactories[id] = null;
    }

    var classPool:ClassPool = getClassPool(FlexLibrarySet.VIEW_POOL, false);
    if (classPool != null) {
      classPool.removeCachedClass(id);
    }
  }

  public function putDocumentFactory(id:int, documentFactory:Object):void {
    var requiredLength:int = id + 1;
    if (documentFactories == null) {
      documentFactories = new Vector.<Object>(requiredLength);
    }
    else if (documentFactories.length < requiredLength) {
      documentFactories.length = requiredLength;
    }

    documentFactories[id] = documentFactory;
  }
}
}
