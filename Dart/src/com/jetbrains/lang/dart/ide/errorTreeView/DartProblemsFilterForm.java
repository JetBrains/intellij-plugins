package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionEvent;
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
  private JBRadioButton myCurrentPackageRadioButton;
  private JBRadioButton myCurrentFileRadioButton;

  private HoverHyperlinkLabel myResetFilterHyperlink;

  private List<FilterListener> myListeners = new ArrayList<>();

  private void createUIComponents() {
    myResetFilterHyperlink = new HoverHyperlinkLabel(DartBundle.message("reset.filter"));
    myResetFilterHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        for (FilterListener listener : myListeners) {
          listener.filtersResetRequested();
        }
      }
    });
  }

  public void reset(@NotNull final DartProblemsFilter filter) {
    myErrorsCheckBox.setSelected(filter.isShowErrors());
    myWarningsCheckBox.setSelected(filter.isShowWarnings());
    myHintsCheckBox.setSelected(filter.isShowHints());

    if (filter.getFileFilterMode() == DartProblemsFilter.FileFilterMode.File) {
      myCurrentFileRadioButton.setSelected(true);
    }
    else if (filter.getFileFilterMode() == DartProblemsFilter.FileFilterMode.Package) {
      myCurrentPackageRadioButton.setSelected(true);
    }
    else if (filter.getFileFilterMode() == DartProblemsFilter.FileFilterMode.ContentRoot) {
      myCurrentContentRootRadioButton.setSelected(true);
    }
    else {
      myWholeProjectRadioButton.setSelected(true);
    }
  }

  public void addListener(@NotNull final FilterListener filterListener) {
    myListeners.add(filterListener);

    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        filterListener.filtersChanged();
      }
    };

    myErrorsCheckBox.addActionListener(listener);
    myWarningsCheckBox.addActionListener(listener);
    myHintsCheckBox.addActionListener(listener);

    myWholeProjectRadioButton.addActionListener(listener);
    myCurrentContentRootRadioButton.addActionListener(listener);
    myCurrentPackageRadioButton.addActionListener(listener);
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

  public DartProblemsFilter.FileFilterMode getFileFilterMode() {
    if (myCurrentFileRadioButton.isSelected()) return DartProblemsFilter.FileFilterMode.File;
    if (myCurrentPackageRadioButton.isSelected()) return DartProblemsFilter.FileFilterMode.Package;
    if (myCurrentContentRootRadioButton.isSelected()) return DartProblemsFilter.FileFilterMode.ContentRoot;
    return DartProblemsFilter.FileFilterMode.All;
  }
}
