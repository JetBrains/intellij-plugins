package jetbrains.plugins.yeoman.projectGenerator.ui.run.controls;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public final class YeomanGeneratorControlUtil {
  public static JComponent buildTitle(@NotNull @Nls String text) {
    return new JBLabel(wrapText(text));
  }

  public static JComponent buildSelectionTitle(@NotNull @Nls String text) {
    return buildTitle(text);
  }

  public static @NlsSafe @NotNull String wrapText(@NotNull @Nls String text) {
    return "<html>" + StringUtil.escapeXmlEntities(text) + "</html>";
  }
}
