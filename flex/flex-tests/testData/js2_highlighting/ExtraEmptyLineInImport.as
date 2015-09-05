package {
import cocoa.border.LinearGradientBorder;

import com.intellij.flex.uiDesigner.plaf.IdeaLookAndFeel;

public class ExtraEmptyLineInImport {
  protected function initialize():void {
    <error>LookAndFeelUtil</error>.initAssets();
  }
}
}
