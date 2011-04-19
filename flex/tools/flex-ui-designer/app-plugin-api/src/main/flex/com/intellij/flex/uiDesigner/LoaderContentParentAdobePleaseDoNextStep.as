package com.intellij.flex.uiDesigner {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.system.Capabilities;
import flash.system.LoaderContext;

internal final class LoaderContentParentAdobePleaseDoNextStep extends Sprite {
  private static const instance:LoaderContentParentAdobePleaseDoNextStep = new LoaderContentParentAdobePleaseDoNextStep();

  public static function configureContext(context:LoaderContext):void {
    // http://juick.com/develar/1320029
    return;
    var version:Array = Capabilities.version.split(' ')[1].split(',');
    var major:Number = parseFloat(version[0]);
    // http://juick.com/develar/1319437
    if (major > 10 || (major == 10 && parseFloat(version[1]) > 1)) {
      context["requestedContentParent"] = instance;
    }
  }

  override public function addChild(child:DisplayObject):DisplayObject {
    return child;
  }

  override public function addChildAt(child:DisplayObject, index:int):DisplayObject {
    return child;
  }

  override public function removeChildAt(index:int):DisplayObject {
    return null;
  }

  override public function removeChild(child:DisplayObject):DisplayObject {
    return child;
  }
}
}
