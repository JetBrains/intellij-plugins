package mx.styles {
import com.intellij.flex.uiDesigner.flex.FlexModuleFactory;

import mx.core.IFlexModuleFactory;

public class StyleManager {
  public static const NOT_A_COLOR:uint = 0xFFFFFFFF;
  
  public static function getStyleManager(moduleFactory:IFlexModuleFactory):IStyleManager2 {
    return FlexModuleFactory(moduleFactory).styleManager;
  }
}
}