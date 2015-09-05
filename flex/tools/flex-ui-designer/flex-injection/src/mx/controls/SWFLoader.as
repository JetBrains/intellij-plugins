package mx.controls {
import com.intellij.flex.uiDesigner.flex.UnknownComponent;

import mx.core.UIComponent;
import mx.core.mx_internal;

use namespace mx_internal;

public class SWFLoader extends FWFLoader {
  override public function loadContent(classOrString:Object):void {
    if (classOrString is String) {
      // todo load real asset
      var shape:UIComponent = new UnknownComponent(classOrString as String);
      shape.width = 100;
      shape.height = 100;
      contentHolder = shape;
      addChild(contentHolder);
      contentLoaded();
    }
    else {
      super.loadContent(classOrString);
    }
  }
}
}