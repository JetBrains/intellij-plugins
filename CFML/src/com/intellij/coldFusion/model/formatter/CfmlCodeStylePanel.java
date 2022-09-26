// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.formatter;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


public class CfmlCodeStylePanel extends CodeStyleAbstractPanel {
  private JPanel myMainPanel;
  private JPanel myPreviewPanel;
  private JCheckBox myAlignKeyValuePairsBox;
  private JCheckBox myAlignAssignments;

  protected CfmlCodeStylePanel(CodeStyleSettings settings) {
    super(settings);
    installPreviewPanel(myPreviewPanel);
    ItemListener listener = new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        somethingChanged();
      }
    };
    myAlignKeyValuePairsBox.addItemListener(listener);
    myAlignAssignments.addItemListener(listener);
  }

  @Override
  protected int getRightMargin() {
    return 80;
  }

  @Override
  protected EditorHighlighter createHighlighter(@NotNull EditorColorsScheme scheme) {
    return EditorHighlighterFactory.getInstance().createEditorHighlighter(new LightVirtualFile("a.cfml"), scheme, null);
  }

  @NotNull
  @Override
  protected FileType getFileType() {
    return CfmlFileType.INSTANCE;
  }

  @Override
  protected String getPreviewText() {
    return """
      <?cfml

      function foo($one, $two = 0, $three = "String")
      {
          $f = "The first value";
          $second = "The second value";
          echo 'Hello, world!';
          $t = "The third value";
          $fourth = "The fourth value";
          if (true) {
              $x = array(
                  0 => "zero",
                  123 => "one two three",
                  25 => "two five"
              );
          }
      }

      ?>""";
  }

  @Override
  public void apply(@NotNull CodeStyleSettings settings) {
    final CfmlCodeStyleSettings cfmlCodeStyleSettings = settings.getCustomSettings(CfmlCodeStyleSettings.class);
    //cfmlCodeStyleSettings.INDENT_CODE_IN_CFML_TAGS = myIndentCodeBox.isSelected();
    cfmlCodeStyleSettings.ALIGN_KEY_VALUE_PAIRS = myAlignKeyValuePairsBox.isSelected();
    cfmlCodeStyleSettings.ALIGN_ASSIGNMENTS = myAlignAssignments.isSelected();
  }

  @Override
  public boolean isModified(CodeStyleSettings settings) {
    final CfmlCodeStyleSettings cfmlCodeStyleSettings = settings.getCustomSettings(CfmlCodeStyleSettings.class);
    return  //cfmlCodeStyleSettings.INDENT_CODE_IN_CFML_TAGS != myIndentCodeBox.isSelected() ||
      cfmlCodeStyleSettings.ALIGN_KEY_VALUE_PAIRS != myAlignKeyValuePairsBox.isSelected() ||
      cfmlCodeStyleSettings.ALIGN_ASSIGNMENTS != myAlignAssignments.isSelected();
  }

  @Override
  public JComponent getPanel() {
    return myMainPanel;
  }

  @Override
  protected void resetImpl(@NotNull CodeStyleSettings settings) {
    final CfmlCodeStyleSettings cfmlCodeStyleSettings = settings.getCustomSettings(CfmlCodeStyleSettings.class);
    //myIndentCodeBox.setSelected(cfmlCodeStyleSettings.INDENT_CODE_IN_CFML_TAGS);
    myAlignKeyValuePairsBox.setSelected(cfmlCodeStyleSettings.ALIGN_KEY_VALUE_PAIRS);
    myAlignAssignments.setSelected(cfmlCodeStyleSettings.ALIGN_ASSIGNMENTS);
  }

  @Override
  public Language getDefaultLanguage() {
    return CfmlLanguage.INSTANCE;
  }
}
