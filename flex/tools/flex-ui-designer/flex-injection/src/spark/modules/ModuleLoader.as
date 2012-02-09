package spark.modules {
import com.intellij.flex.uiDesigner.flex.UnknownComponentHelper;

import flash.utils.ByteArray;

import spark.components.ResizeMode;

public class ModuleLoader extends FoduleLoader {
  private const unknownComponentHelper:UnknownComponentHelper = new UnknownComponentHelper();

  override public function loadModule(url:String = null, bytes:ByteArray = null):void {
  }

  override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
    super.updateDisplayList(unscaledWidth, unscaledHeight);

    unknownComponentHelper.draw((resizeMode == ResizeMode.SCALE) ? measuredWidth : unscaledWidth, (resizeMode == ResizeMode.SCALE) ? measuredHeight : unscaledHeight, this, "ModuleLoader " + (url == null || url.length == 0 ? "<empty URL>" : url));
  }
}
}