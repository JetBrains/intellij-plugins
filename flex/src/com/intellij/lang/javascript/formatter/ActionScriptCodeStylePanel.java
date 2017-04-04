package com.intellij.lang.javascript.formatter;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
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
    super(JavaScriptSupportLoader.ECMA_SCRIPT_L4, settings);
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
    @NonNls String baseName = "field";
    @NonNls String propertyName = baseName;
    @NonNls String varName =  baseName;

    return "/*\n" +
           "    Multiline\n" +
           "        C-style\n" +
           "            Comment\n" +
           "*/\n" +
           "package aaa {\nclass XXX {\n" +
           "private var " + varName + ":int" + "\n" +
           "function get " +  propertyName + "():int {\nreturn " + varName + "}\n" +
           "function set " +  propertyName + "(val:int):void {\n" +
           " var myLink = {\n" +
           "    img : \"btn.gif\",\n" +
           "    text : \"Button\",\n" +
           "    width : 128\n" +
           "}" +
           "varName = val" + "\n" +
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
