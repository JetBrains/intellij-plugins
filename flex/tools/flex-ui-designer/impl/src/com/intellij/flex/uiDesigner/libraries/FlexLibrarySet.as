package com.intellij.flex.uiDesigner.libraries {
import com.intellij.flex.uiDesigner.ClassPool;

public final class FlexLibrarySet extends LibrarySet {
  public static const SWF_POOL:String = "_s";
  public static const IMAGE_POOL:String = "_b";
  public static const VIEW_POOL:String = "_v";

  private var _s:ClassPool;
  private var _b:ClassPool;
  private var _v:ClassPool;

  public function FlexLibrarySet(id:int, parent:LibrarySet) {
    super(id, parent);
  }

  public function getClassPool(id:String, createIfNeed:Boolean = true):ClassPool {
    if (createIfNeed && this[id] == null) {
      this[id] = new ClassPool(id, this);
    }
    return this[id];
  }
}
}
