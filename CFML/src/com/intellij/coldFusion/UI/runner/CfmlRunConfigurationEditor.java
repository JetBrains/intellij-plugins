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
package com.intellij.coldFusion.UI.runner;

import com.intellij.ide.browsers.BrowserSelector;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;

import static com.intellij.coldFusion.UI.runner.CfmlRunnerParameters.WWW_ROOT;

public class CfmlRunConfigurationEditor extends SettingsEditor<CfmlRunConfiguration> {
  private JPanel myMainPanel;
  private JTextField myWebPathField;
  private JTextField myPagePathField;
  private JPanel myBrowserSelectorPanel;
  private JLabel myPageURLLabel;
  private JLabel myServerURLLabel;
  private final BrowserSelector myBrowserSelector;

  private final String INDEX_CFM = "/index.cfm";
  private boolean syncServerAndPageUrl = true;

  public CfmlRunConfigurationEditor() {
    myBrowserSelector = new BrowserSelector();
    myBrowserSelectorPanel.add(BorderLayout.CENTER, myBrowserSelector.getMainComponent());
    if (syncServerAndPageUrl) {
      myWebPathField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          updatePagePath("");
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          updatePagePath("");
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          updatePagePath("");
        }
      });
    }
  }

  public CfmlRunConfigurationEditor(final CfmlRunConfiguration configuration) {
    myBrowserSelector = new BrowserSelector();
    myBrowserSelectorPanel.add(BorderLayout.CENTER, myBrowserSelector.getMainComponent());
    if (syncServerAndPageUrl) {
      myWebPathField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          updatePagePath(configuration);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          updatePagePath(configuration);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          updatePagePath(configuration);
        }
      });
    }
  }

  private void updatePagePath(@Nullable CfmlRunConfiguration configuration) {
    if (configuration != null) {
      updatePagePath(configuration.getRunnerParameters().getPageUrl());
    } else {
      updatePagePath("");
    }
  }

  public boolean isSyncServerAndPageUrl() {
    return syncServerAndPageUrl;
  }

  public void setSyncServerAndPageUrl(boolean syncServerAndPageUrl) {
    this.syncServerAndPageUrl = syncServerAndPageUrl;
  }

  private void updatePagePath(String url)  {
    try {
      String text = myWebPathField.getDocument().getText(0, myWebPathField.getDocument().getLength());
      String appendUrl = (url.equals("") ? getRelativeProjectPath() : url);
      myPagePathField.setText(text + appendUrl);
    }
    catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void resetEditorFrom(CfmlRunConfiguration s) {
    CfmlRunnerParameters params = s.getRunnerParameters();
    myWebPathField.setText(params.getUrl());
    myBrowserSelector.setSelected(params.getNonDefaultBrowser() != null ? params.getNonDefaultBrowser() : null);
  }

  @Override
  protected void applyEditorTo(CfmlRunConfiguration s) throws ConfigurationException {
    CfmlRunnerParameters params = s.getRunnerParameters();
    String webPath = myWebPathField.getText();
    String myPagePathFieldText = myPagePathField.getText();
    int indexOfWebPath = myPagePathFieldText.indexOf(webPath);
    String pagePath;
    if (indexOfWebPath != -1) {
      pagePath = myPagePathFieldText.substring(indexOfWebPath + webPath.length());
    } else {
      pagePath = myPagePathFieldText;
    }
    params.setUrl(myWebPathField.getText());
    params.setPageUrl(pagePath);
    params.setNonDefaultBrowser(myBrowserSelector.getSelected());

  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myMainPanel;
  }

  /**
   *
   * @return relative path from <b>wwwroot</b> folder to index.cfm in project.
   */
  public String getRelativeProjectPath() {
    Project project = com.intellij.openapi.project.ProjectUtil.guessCurrentProject(myMainPanel);

    VirtualFile projectBaseDir = project.getBaseDir();
    String projectPath = projectBaseDir.getPath();
    int wwwrootIndex = projectPath.lastIndexOf(WWW_ROOT);
    if (wwwrootIndex == -1) {
      return projectBaseDir.getName() + INDEX_CFM;
    }
    String substring = projectPath.substring(wwwrootIndex + WWW_ROOT.length());
    return substring + INDEX_CFM;
  }
}
