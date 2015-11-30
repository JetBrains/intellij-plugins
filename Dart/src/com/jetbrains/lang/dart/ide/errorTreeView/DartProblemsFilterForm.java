package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DartProblemsFilterForm {

  private JPanel myMainPanel;

  private JBCheckBox myErrorsCheckBox;
  private JBCheckBox myWarningsCheckBox;
  private JBCheckBox myHintsCheckBox;

  private JBRadioButton myWholeProjectRadioButton;
  private JBRadioButton myCurrentPackageRadioButton;
  private JBRadioButton myCurrentFileRadioButton;

  private HoverHyperlinkLabel myResetFiltersHyperlink;

  private List<FilterListener> myListeners = new ArrayList<FilterListener>();

  private void createUIComponents() {
    myResetFiltersHyperlink = new HoverHyperlinkLabel("Reset all filters");
    myResetFiltersHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        for (FilterListener listener : myListeners) {
          listener.filtersResetRequested();
        }
      }
    });
  }

  public void reset(final boolean showErrors,
                    final boolean showWarnings,
                    final boolean showHints,
                    @NotNull final DartProblemsViewPanel.FileFilterMode fileFilterMode) {
    myErrorsCheckBox.setSelected(showErrors);
    myWarningsCheckBox.setSelected(showWarnings);
    myHintsCheckBox.setSelected(showHints);

    if (fileFilterMode == DartProblemsViewPanel.FileFilterMode.File) {
      myCurrentFileRadioButton.setSelected(true);
    }
    else if (fileFilterMode == DartProblemsViewPanel.FileFilterMode.Package) {
      myCurrentPackageRadioButton.setSelected(true);
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

  public DartProblemsViewPanel.FileFilterMode getFileFilterMode() {
    if (myCurrentFileRadioButton.isSelected()) return DartProblemsViewPanel.FileFilterMode.File;
    if (myCurrentPackageRadioButton.isSelected()) return DartProblemsViewPanel.FileFilterMode.Package;
    return DartProblemsViewPanel.FileFilterMode.All;
  }

  interface FilterListener {
    void filtersChanged();

    void filtersResetRequested();
  }
}
