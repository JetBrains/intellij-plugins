package com.intellij.flex.uiDesigner.designSurface {
public final class DesignSurfaceDataKeys {
  public static var LAYOUT_MANAGER:LayoutManagerDataKey;

  internal static function burnInHellAdobe():void {
    LAYOUT_MANAGER = new LayoutManagerDataKey();
  }
}

DesignSurfaceDataKeys.burnInHellAdobe();
}

import com.intellij.flex.uiDesigner.designSurface.LayoutManager;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;

class LayoutManagerDataKey extends DataKey {
  public function getData(dataContext:DataContext):LayoutManager {
    return LayoutManager(dataContext.getData(this));
  }
}