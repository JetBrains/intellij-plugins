package com.intellij.flex.uiDesigner.designSurface {
import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.geom.Point;

import org.jetbrains.actionSystem.DataContext;

[Abstract]
public class DocumentTool implements Tool {
  protected function get displayObject():DisplayObject {
    throw new Error("abstract");
  }

  public function activate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext):void {
    var documentDisplayManager:DocumentDisplayManager = DesignSurfaceDataKeys.DOCUMENT_DISPLAY_MANAGER.getData(dataContext);
    var layoutManager:LayoutManager = documentDisplayManager.layoutManager;
    if (documentDisplayManager.layoutManager == null) {
      deactivate();
      return;
    }

    var canvas:DisplayObject = displayObject;
    if (canvas == null) {
      canvas = createCanvas();
    }
    else {
      canvas.visible = true;
    }

    if (canvas.parent != displayObjectContainer) {
      displayObjectContainer.addChild(canvas);
    }

    var location:Point = areaLocations[AreaLocations.BODY];
    canvas.x = location.x;
    canvas.y = location.y;

    doActivate(displayObjectContainer, areaLocations, dataContext, documentDisplayManager);
  }

  protected function doActivate(displayObjectContainer:DisplayObjectContainer, areaLocations:Vector.<Point>, dataContext:DataContext, documentDisplayManager:DocumentDisplayManager):void {
    throw new Error("abstract");
  }

  protected function createCanvas():DisplayObject {
    throw new Error("abstract");
  }

  public function deactivate():void {
    if (displayObject != null) {
      displayObject.visible = false;
    }
  }
}
}
