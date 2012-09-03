package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class TestMethodRunSettingsSection extends AbstractRunSettingsSection {

  private final TestCaseRunSettingsSection myTestCaseRunSettingsSection;
  private final JComboBox myTestMethodNameComboBox;
  private final JBLabel myLabel;

  TestMethodRunSettingsSection() {
    myTestCaseRunSettingsSection = new TestCaseRunSettingsSection(this);
    myTestMethodNameComboBox = createComboBox();
    myLabel = new JBLabel("Method:");
    setAnchor(SwingUtils.getWiderComponent(myLabel, myTestCaseRunSettingsSection));
  }

  private String getTestMethodName() {
    Object value = myTestMethodNameComboBox.getSelectedItem();
    return value == null ? "" : value.toString();
  }

  public void stateChanged(@NotNull Project project,
                           @NotNull String jsTestFilePath,
                           @NotNull String testCaseName) {
    String oldValue = getTestMethodName();
    try {
      updateTestMethodVariants(project, jsTestFilePath, testCaseName);
    }
    finally {
      myTestMethodNameComboBox.setSelectedItem(oldValue);
    }
  }

  private void updateTestMethodVariants(@NotNull Project project,
                                        @NotNull String jsTestFilePath,
                                        @NotNull String testCaseName) {
    myTestMethodNameComboBox.removeAllItems();
    VirtualFile jsTestVirtualFile = VfsUtil.findFileByIoFile(new File(jsTestFilePath), false);
    if (jsTestVirtualFile == null) {
      return;
    }
    JSFile jsFile = ObjectUtils.tryCast(PsiManager.getInstance(project).findFile(jsTestVirtualFile), JSFile.class);
    if (jsFile == null) {
      return;
    }
    TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsFile);
    if (pack != null) {
      List<String> testMethodNames = pack.getChildrenOf(testCaseName);
      for (String testMethodName : testMethodNames) {
        myTestMethodNameComboBox.addItem(testMethodName);
      }
    }
  }

  private static JComboBox createComboBox() {
    JComboBox comboBox = new JComboBox();
    comboBox.setRenderer(new ListCellRendererWrapper<String>() {
      @Override
      public void customize(JList list, String value, int index, boolean selected, boolean hasFocus) {
        setText(value);
      }
    });
    comboBox.setEditable(true);
    return comboBox;
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myTestCaseRunSettingsSection.resetFrom(runSettings);
    myTestMethodNameComboBox.setSelectedItem(runSettings.getTestMethodName());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myTestCaseRunSettingsSection.applyTo(runSettingsBuilder);
    String testMethodName = getTestMethodName();
    runSettingsBuilder.setTestMethodName(testMethodName);
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
        0, 0,
        2, 1,
        0.0, 0.0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
      );
      JComponent testCaseComponent = myTestCaseRunSettingsSection.getComponent(creationContext);
      panel.add(testCaseComponent, c);
    }

    {
      myLabel.setDisplayedMnemonic('M');
      myLabel.setLabelFor(myTestMethodNameComboBox);
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      GridBagConstraints c = new GridBagConstraints(
        0, 1,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
        0, 0
      );
      panel.add(myLabel, c);
    }
    {
      GridBagConstraints c = new GridBagConstraints(
        1, 1,
        1, 1,
        1.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
        0, 0
      );
      panel.add(myTestMethodNameComboBox, c);
      myLabel.setLabelFor(myTestMethodNameComboBox);
    }
    SwingUtils.addGreedyBottomRow(panel);
    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
    myTestCaseRunSettingsSection.setAnchor(anchor);
  }

}
