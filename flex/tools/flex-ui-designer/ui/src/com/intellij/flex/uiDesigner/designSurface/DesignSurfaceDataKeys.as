package com.intellij.flex.uiDesigner.designSurface {
public final class DesignSurfaceDataKeys {
  public static var DOCUMENT_DISPLAY_MANAGER:DocumentDisplayManagerDataKey;

  internal static function burnInHellAdobe():void {
    DOCUMENT_DISPLAY_MANAGER = new DocumentDisplayManagerDataKey();
  }
}

DesignSurfaceDataKeys.burnInHellAdobe();
}

import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;

class DocumentDisplayManagerDataKey extends DataKey {
  public function getData(dataContext:DataContext):DocumentDisplayManager {
    return DocumentDisplayManager(dataContext.getData(this));
  }
}