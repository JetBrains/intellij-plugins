package mx.styles {
import com.intellij.flex.uiDesigner.flex.FlexModuleFactory;

import mx.core.IFlexModule;
import mx.core.IFlexModuleFactory;
import mx.managers.LayoutManager;

public class StyleManager {
  public static const NOT_A_COLOR:uint = 0xFFFFFFFF;

  // IDEA-7133, see VolumeBar, dropDownController created in constructor, so getStyle called in constructor (i. e. moduleFactory is null)
  public static var tempStyleManagerForTalentAdobeEngineers:IStyleManager2;
  
  public static function getStyleManager(moduleFactory:IFlexModuleFactory):IStyleManager2 {
    if (moduleFactory != null) {
      return FlexModuleFactory(moduleFactory).styleManager;
    }
    else if (tempStyleManagerForTalentAdobeEngineers != null) {
      return tempStyleManagerForTalentAdobeEngineers;
    }
    else {
      // IDEA-71741
      return FlexModuleFactory(IFlexModule(LayoutManager.getInstance().getCurrentObject()).moduleFactory).styleManager;
    }
  }
}
}