package spark.components.supportClasses {
import com.intellij.flex.uiDesigner.flex.SkinHost;
import com.intellij.flex.uiDesigner.flex.SkinPartFinder;

import mx.core.IFactory;

public class SkinnableComponent extends FkinnableComponent implements SkinHost {
  override protected function findSkinParts():void {
    var sp:Object = skinParts;
    if (sp == null) {
      return;
    }

    var skinPartFinder:SkinPartFinder = getStyle("skinFactory") as SkinPartFinder;
    if (skinPartFinder != null) {
      skinPartFinder.findSkinParts(this, sp);
      // why we don't stop method execution? Because skinFactory class may extends from library class
      // (i.e. part of skin parts are defined in MXML Project Component and part of skin parts are defined in AS Library class)
    }

    for (var partName:String in sp) {
      if (partName in skin) {
        this[partName] = skin[partName];
        // develar: we don't use skin[partName] as instance value, because we want use component setter/getter
        // it is flex related stuff, optimization may be dangerous
        var instance:Object = this[partName];
        if (instance != null && !(instance is IFactory)) {
          partAdded(partName, instance);
        }
      }
    }
  }

  public function skinPartAdded(partName:String, instance:Object):void {
    this[partName] = instance;
    partAdded(partName, instance);
  }
}
}