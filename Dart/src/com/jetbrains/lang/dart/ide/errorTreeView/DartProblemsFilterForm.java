// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DartProblemsFilterForm {

  interface FilterListener {
    void filtersChanged();

    void filtersResetRequested();
  }


  private JPanel myMainPanel;

  private JBCheckBox myErrorsCheckBox;
  private JBCheckBox myWarningsCheckBox;
  private JBCheckBox myHintsCheckBox;

  private JBRadioButton myWholeProjectRadioButton;
  private JBRadioButton myCurrentContentRootRadioButton;
  private JBRadioButton myCurrentDartPackageRadioButton;
  private JBRadioButton myCurrentDirectoryRadioButton;
  private JBRadioButton myCurrentFileRadioButton;

  private HoverHyperlinkLabel myResetFilterHyperlink;

  private final List<FilterListener> myListeners = new ArrayList<>();

  private void createUIComponents() {
    myResetFilterHyperlink = new HoverHyperlinkLabel(DartBundle.message("dart.problems.filter.popup.link.reset.filter"));
    myResetFilterHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        myListeners.forEach(FilterListener::filtersResetRequested);
      }
    });
  }

  public void reset(@NotNull final DartProblemsPresentationHelper presentationHelper) {
    myErrorsCheckBox.setSelected(presentationHelper.isShowErrors());
    myWarningsCheckBox.setSelected(presentationHelper.isShowWarnings());
    myHintsCheckBox.setSelected(presentationHelper.isShowHints());

    if (presentationHelper.getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.File) {
      myCurrentFileRadioButton.setSelected(true);
    }
    else if (presentationHelper.getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.Directory) {
      myCurrentDirectoryRadioButton.setSelected(true);
    }
    else if (presentationHelper.getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.DartPackage) {
      myCurrentDartPackageRadioButton.setSelected(true);
    }
    else if (presentationHelper.getFileFilterMode() == DartProblemsViewSettings.FileFilterMode.ContentRoot) {
      myCurrentContentRootRadioButton.setSelected(true);
    }
    else {
      myWholeProjectRadioButton.setSelected(true);
    }
  }

  public void addListener(@NotNull final FilterListener filterListener) {
    myListeners.add(filterListener);

    final ActionListener listener = e -> filterListener.filtersChanged();

    myErrorsCheckBox.addActionListener(listener);
    myWarningsCheckBox.addActionListener(listener);
    myHintsCheckBox.addActionListener(listener);

    myWholeProjectRadioButton.addActionListener(listener);
    myCurrentContentRootRadioButton.addActionListener(listener);
    myCurrentDartPackageRadioButton.addActionListener(listener);
    myCurrentDirectoryRadioButton.addActionListener(listener);
    myCurrentFileRadioButton.addActionListener(listener);
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public boolean isShowErrors() {
    return myErrorsCheckBox.isSelected();
  }

  public boolean isShowWarnings() {
    return myWarningsCheckBox.isSelected();
  }

  public boolean isShowHints() {
    return myHintsCheckBox.isSelected();
  }

  @NotNull
  public DartProblemsViewSettings.FileFilterMode getFileFilterMode() {
    if (myCurrentFileRadioButton.isSelected()) return DartProblemsViewSettings.FileFilterMode.File;
    if (myCurrentDirectoryRadioButton.isSelected()) return DartProblemsViewSettings.FileFilterMode.Directory;
    if (myCurrentDartPackageRadioButton.isSelected()) return DartProblemsViewSettings.FileFilterMode.DartPackage;
    if (myCurrentContentRootRadioButton.isSelected()) return DartProblemsViewSettings.FileFilterMode.ContentRoot;
    return DartProblemsViewSettings.FileFilterMode.All;
  }
}
