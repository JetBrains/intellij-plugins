/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */

public class CfmlCodeStylePanel extends CodeStyleAbstractPanel {
  private JPanel myMainPanel;
  private JPanel myPreviewPanel;
  private JCheckBox myAlignKeyValuePairsBox;
  private JCheckBox myAlignAssignments;

  protected CfmlCodeStylePanel(CodeStyleSettings settings) {
    super(settings);
    installPreviewPanel(myPreviewPanel);
    ItemListener listener = new ItemListener() {
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
  protected EditorHighlighter createHighlighter(EditorColorsScheme scheme) {
    return EditorHighlighterFactory.getInstance().createEditorHighlighter(new LightVirtualFile("a.cfml"), scheme, null);
  }

  @NotNull
  @Override
  protected FileType getFileType() {
    return CfmlFileType.INSTANCE;
  }

  @Override
  protected String getPreviewText() {
    return "<?cfml\n" +
           "\n" +
           "function foo($one, $two = 0, $three = \"String\")\n" +
           "{\n" +
           "    $f = \"The first value\";\n" +
           "    $second = \"The second value\";\n" +
           "    echo 'Hello, world!';\n" +
           "    $t = \"The third value\";\n" +
           "    $fourth = \"The fourth value\";\n" +
           "    if (true) {\n" +
           "        $x = array(\n" +
           "            0 => \"zero\",\n" +
           "            123 => \"one two three\",\n" +
           "            25 => \"two five\"\n" +
           "        );\n" +
           "    }\n" +
           "}\n" +
           "\n" +
           "?>";
  }

  @Override
  public void apply(CodeStyleSettings settings) {
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
  protected void resetImpl(CodeStyleSettings settings) {
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
