package com.intellij.lang.javascript.formatter;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeEditorHighlighterProviders;
import com.intellij.openapi.project.ProjectUtil;
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


  @NotNull
  @Override
  protected FileType getFileType() {
    return ActionScriptFileType.INSTANCE;
  }

  @Override
  protected EditorHighlighter createHighlighter(final EditorColorsScheme scheme) {
    return FileTypeEditorHighlighterProviders.INSTANCE.forFileType(getFileType())
      .getEditorHighlighter(ProjectUtil.guessCurrentProject(getPanel()), getFileType(), null, scheme);
  }

  @Override
  protected String getPreviewText() {
    final JSCodeStyleSettings jsCodeStyleSettings = getCustomJSSettings(getSettings());
    @NonNls String baseName = "field";
    @NonNls String propertyName = (myPropertyPrefixTextField != null ? myPropertyPrefixTextField.getText() : jsCodeStyleSettings.PROPERTY_PREFIX) + baseName;
    @NonNls String varName = (myFieldPrefixTextField != null ? myFieldPrefixTextField.getText() : jsCodeStyleSettings.FIELD_PREFIX) + baseName;
    @NonNls String semiColon = (myUseSemicolon != null ? myUseSemicolon.isSelected() : jsCodeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT) ? ";":"";

    return "/*\n" +
           "    Multiline\n" +
           "        C-style\n" +
           "            Comment\n" +
           "*/\n" +
           "package aaa {\nclass XXX {\n" +
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
