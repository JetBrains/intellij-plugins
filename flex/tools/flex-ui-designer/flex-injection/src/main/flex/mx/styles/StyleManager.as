package mx.styles {
import com.intellij.flex.uiDesigner.flex.FlexModuleFactory;

import mx.core.IFlexModuleFactory;

public class StyleManager {
  public static const NOT_A_COLOR:uint = 0xFFFFFFFF;

  // IDEA-7133, see VolumeBar, dropDownController created in constructor, so getStyle called in constructor (i. e. moduleFactory is null)
  public static var tempStyleManagerForTalentAdobeEngineers:IStyleManager2;
  
  public static function getStyleManager(moduleFactory:IFlexModuleFactory):IStyleManager2 {
    return moduleFactory == null ? tempStyleManagerForTalentAdobeEngineers : FlexModuleFactory(moduleFactory).styleManager;
  }
}
}