/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package jetbrains.plugins.yeoman.projectGenerator.ui.list;

import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.speedSearch.SpeedSearchSupply;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.text.Matcher;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class YeomanGeneratorCellRenderer extends DefaultTableCellRenderer {
  private final YeomanGeneratorInfo myInfo;

  private JPanel myPanel;
  private SimpleColoredComponent myName;
  private JPanel myInfoPanel;
  private JBLabel myStatus;

  public YeomanGeneratorCellRenderer(YeomanGeneratorInfo info) {
    myInfo = info;
    myName.setFont(StartupUiUtil.getLabelFont());

    myPanel.setBorder(UIUtil.isRetina() ? JBUI.Borders.empty(2, 1) : JBUI.Borders.empty(1));
  }


  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (myInfo != null) {
      Color fg = UIUtil.getTableForeground(isSelected, hasFocus);
      Color bg = UIUtil.getTableBackground(isSelected, hasFocus);

      myPanel.setBackground(bg);

      myName.setForeground(fg);

      myName.clear();
      myName.setOpaque(false);
      String generatorName = myInfo.getYoName() + "  ";
      Object query = table.getClientProperty(SpeedSearchSupply.SEARCH_QUERY_KEY);
      SimpleTextAttributes attr = new SimpleTextAttributes(UIUtil.getListBackground(isSelected, hasFocus),
                                                           UIUtil.getListForeground(isSelected, hasFocus),
                                                           JBColor.RED,
                                                           SimpleTextAttributes.STYLE_PLAIN);
      Matcher matcher = NameUtil.buildMatcher("*" + query, NameUtil.MatchingCaseSensitivity.NONE);
      if (query instanceof String) {
        SpeedSearchUtil.appendColoredFragmentForMatcher(generatorName, myName, attr, matcher, UIUtil.getTableBackground(isSelected, hasFocus), true);
      }
      else {
        myName.append(generatorName);
      }
    }
    return myPanel;
  }
}
