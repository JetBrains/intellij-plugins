package com.intellij.lang.javascript.formatter;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Rustam Vishnyakov
 */
public class ActionScriptCodeStylePanel extends JSCodeStylePanel {



  public ActionScriptCodeStylePanel(final CodeStyleSettings settings) {
    super(settings);
    mySpaceAfterTypeRefColonCheckBox.setVisible(true);
    mySpaceBeforeTypeRefColonCheckBox.setVisible(true);
    myNamingConventionsPane.setVisible(true);
    myFormattingOptionsPane.setVisible(true);
    myIndentPackageChildren.setVisible(true);
  }

  @Override
  protected String getPreviewText() {
    final JSCodeStyleSettings jsCodeStyleSettings = getCustomJSSettings(getSettings());
    @NonNls String baseName = "field";
    @NonNls String propertyName = (myPropertyPrefixTextField != null ? myPropertyPrefixTextField.getText() : jsCodeStyleSettings.PROPERTY_PREFIX) + baseName;
    @NonNls String varName = (myFieldPrefixTextField != null ? myFieldPrefixTextField.getText() : jsCodeStyleSettings.FIELD_PREFIX) + baseName;
    @NonNls String semiColon = (myUseSemicolon != null ? myUseSemicolon.isSelected() : jsCodeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT) ? ";":"";

    return "package aaa {\nclass XXX {\n" +
           "private var " + varName + ":int" + semiColon + "\n" +
           "function get " +  propertyName + "():int {\nreturn " + varName +  semiColon + "}\n" +
           "function set " +  propertyName + "(val:int):void {\n" +
           " var myLink = {\n" +
           "    img : \"btn.gif\",\n" +
           "    text : \"Button\",\n" +
           "    width : 128\n" +
           "}" +
           "varName = val" + semiColon + "\n" +
           "}\n" +
           "}";
  }

  @Override
  protected JSCodeStyleSettings getCustomJSSettings(CodeStyleSettings settings) {
    return settings.getCustomSettings(ECMA4CodeStyleSettings.class);
  }

  @Override
  protected String getFileTypeExtension(FileType fileType) {
    return "as";
  }
}
