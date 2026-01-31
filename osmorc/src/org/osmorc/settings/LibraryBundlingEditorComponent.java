/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.settings;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ListUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class LibraryBundlingEditorComponent {
  private final Project myProject;
  private JPanel myMainPanel;
  private JPanel myRulesPanel;
  private JBList<LibraryBundlificationRule> myRulesList;
  private JTextField myLibraryRegex;
  private ManifestEditor myManifestEditor;
  private JCheckBox myNeverBundle;
  private JCheckBox myStopAfterThisRule;

  private final CollectionListModel<LibraryBundlificationRule> myRulesModel;
  private int myLastSelected = -1;

  public LibraryBundlingEditorComponent(@NotNull Project project) {
    myProject = project;
    GuiUtils.replaceJSplitPaneWithIDEASplitter(myMainPanel);
    ((JBSplitter)myMainPanel.getComponent(0)).setProportion(0.4f);

    myRulesPanel.add(
      ToolbarDecorator.createDecorator(myRulesList)
        .setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            updateCurrentRule();
            myRulesModel.add(new LibraryBundlificationRule());
            myRulesList.setSelectedIndex(myRulesModel.getSize() - 1);
            updateFields();
          }
        })
        .setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            myLastSelected = -1;
            if (myRulesModel.getSize() == 1) {
              myRulesModel.setElementAt(new LibraryBundlificationRule(), 0);
              myRulesList.setSelectedIndex(0);
            }
            else {
              int index = myRulesList.getSelectedIndex();
              myRulesModel.remove(index);
              myRulesList.setSelectedIndex(index > 0 ? index - 1 : 0);
            }
            updateFields();
          }
        })
        .setMoveUpAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            updateCurrentRule();
            myLastSelected = -1;
            ListUtil.moveSelectedItemsUp(myRulesList);
            updateFields();
          }
        })
        .setMoveDownAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            updateCurrentRule();
            myLastSelected = -1;
            ListUtil.moveSelectedItemsDown(myRulesList);
            updateFields();
          }
        })
        .addExtraAction(new DumbAwareAction(IdeBundle.message("button.copy"), null, PlatformIcons.COPY_ICON) {
          @Override
          public void actionPerformed(@NotNull AnActionEvent e) {
            updateCurrentRule();
            int index = myRulesList.getSelectedIndex();
            if (index >= 0) {
              myRulesModel.add(myRulesModel.getElementAt(index).copy());
              myRulesList.setSelectedIndex(myRulesModel.getSize() - 1);
              updateFields();
            }
          }

          @Override
          public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(myRulesList.getSelectedIndex() >= 0);
          }

          @Override
          public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
          }
        })
        .createPanel(), BorderLayout.CENTER
    );

    myRulesModel = new CollectionListModel<>();
    myRulesList.setModel(myRulesModel);
    myRulesList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        updateCurrentRule();
        updateFields();
      }
    });
  }

  private void createUIComponents() {
    myManifestEditor = new ManifestEditor(myProject, "");
  }

  private void updateFields() {
    int index = myRulesList.getSelectedIndex();
    if (index >= 0 && index != myLastSelected) {
      final LibraryBundlificationRule rule = myRulesModel.getElementAt(index);
      myLibraryRegex.setText(rule.getRuleRegex());
      UIUtil.invokeLaterIfNeeded(() -> myManifestEditor.setText(rule.getAdditionalProperties()));
      myNeverBundle.setSelected(rule.isDoNotBundle());
      myStopAfterThisRule.setSelected(rule.isStopAfterThisRule());
      myLastSelected = index;
    }
    myLibraryRegex.setEnabled(index >= 0);
    myManifestEditor.setEnabled(index >= 0);
    myNeverBundle.setEnabled(index >= 0);
    myStopAfterThisRule.setEnabled(index >= 0);
  }

  private void updateCurrentRule() {
    if (myLastSelected >= 0 && myLastSelected < myRulesModel.getSize()) {
      LibraryBundlificationRule newRule = new LibraryBundlificationRule();
      newRule.setRuleRegex(myLibraryRegex.getText().trim());
      newRule.setAdditionalProperties(myManifestEditor.getText().trim());
      newRule.setDoNotBundle(myNeverBundle.isSelected());
      newRule.setStopAfterThisRule(myStopAfterThisRule.isSelected());
      if (!newRule.equals(myRulesModel.getElementAt(myLastSelected))) {
        myRulesModel.setElementAt(newRule, myLastSelected);
      }
    }
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void dispose() {
    Disposer.dispose(myManifestEditor);
    myManifestEditor = null;
  }

  public boolean isModified(ApplicationSettings settings) {
    updateCurrentRule();
    List<LibraryBundlificationRule> rules = settings.getLibraryBundlificationRules();
    if (myRulesModel.getSize() != rules.size()) {
      return true;
    }
    for (int i = 0; i < rules.size(); i++) {
      if (!rules.get(i).equals(myRulesModel.getElementAt(i))) {
        return true;
      }
    }
    return false;
  }

  public void resetTo(ApplicationSettings settings) {
    myLastSelected = -1;
    myRulesModel.replaceAll(settings.getLibraryBundlificationRules());
    myRulesList.setSelectedIndex(0);
    updateFields();
  }

  public void applyTo(ApplicationSettings settings) throws ConfigurationException {
    updateCurrentRule();

    for (int i = 0; i < myRulesModel.getSize(); i++) {
      try {
        myRulesModel.getElementAt(i).validate();
      }
      catch (IllegalArgumentException e) {
        myRulesList.setSelectedIndex(i);
        throw new ConfigurationException(e.getMessage());
      }
    }

    settings.setLibraryBundlificationRules(new ArrayList<>(myRulesModel.getItems()));
  }
}
