package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ActionScriptIndentOptionsEditor extends SmartIndentOptionsEditor {
  protected JCheckBox myIndentPackageChildren;

  @Override
  protected void addComponents() {
    super.addComponents();
    myIndentPackageChildren = new JCheckBox(FlexBundle.message("to.indent.package.statement.children"));
    add(myIndentPackageChildren);
  }

  @Override
  public boolean isModified(CodeStyleSettings settings, CommonCodeStyleSettings.IndentOptions options) {
    JSCodeStyleSettings jsSettings = getCustomJSSettings(settings);
    return super.isModified(settings, options) ||
           (jsSettings.INDENT_PACKAGE_CHILDREN == JSCodeStyleSettings.INDENT) != myIndentPackageChildren.isSelected();
  }

  @Override
  public void apply(CodeStyleSettings settings, CommonCodeStyleSettings.IndentOptions options) {
    super.apply(settings, options);
    final JSCodeStyleSettings jsCodeStyleSettings = getCustomJSSettings(settings);
    jsCodeStyleSettings.INDENT_PACKAGE_CHILDREN =
      myIndentPackageChildren.isSelected() ? JSCodeStyleSettings.INDENT : JSCodeStyleSettings.DO_NOT_INDENT;
  }

  private static ECMA4CodeStyleSettings getCustomJSSettings(CodeStyleSettings settings) {
    return settings.getCustomSettings(ECMA4CodeStyleSettings.class);
  }

  @Override
  public void reset(@NotNull CodeStyleSettings settings, @NotNull CommonCodeStyleSettings.IndentOptions options) {
    super.reset(settings, options);
    myIndentPackageChildren.setSelected(getCustomJSSettings(settings).INDENT_PACKAGE_CHILDREN == JSCodeStyleSettings.INDENT);
  }
}
