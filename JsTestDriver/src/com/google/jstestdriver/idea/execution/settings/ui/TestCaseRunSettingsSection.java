package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.google.jstestdriver.idea.util.TextChangeListener;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

public class TestCaseRunSettingsSection extends AbstractRunSettingsSection {

  private final JsFileRunSettingsSection myJsFileRunSettingsSection;
  private final JComboBox myTestCaseNameComboBox;
  private final JBLabel myLabel;
  private final TestMethodRunSettingsSection myTestMethodSettingsSection;

  TestCaseRunSettingsSection() {
    this(null);
  }

  TestCaseRunSettingsSection(@Nullable TestMethodRunSettingsSection testMethodSettingsSection) {
    myTestMethodSettingsSection = testMethodSettingsSection;
    myJsFileRunSettingsSection = new JsFileRunSettingsSection();
    myTestCaseNameComboBox = createComboBox();
    myLabel = new JBLabel("Case:");
    setAnchor(SwingUtils.getWiderComponent(myLabel, myJsFileRunSettingsSection));
  }

  private void trackJsTestFilePathChanges(@NotNull final Project project) {
    SwingUtils.addTextChangeListener(
      myJsFileRunSettingsSection.getJsTestFileTextField(),
      new TextChangeListener() {
        @Override
        public void textChanged(String oldJsTestFilePath, @NotNull String newJsTestFilePath) {
          String oldValue = getTestCaseName();
          try {
            updateTestCaseVariants(project, newJsTestFilePath);
            fireStateChanged(project);
          }
          finally {
            myTestCaseNameComboBox.setSelectedItem(oldValue);
          }
        }
      }
    );
    myTestCaseNameComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        fireStateChanged(project);
      }
    });
  }

  private void fireStateChanged(@NotNull Project project) {
    String jsTestFilePath = myJsFileRunSettingsSection.getJsTestFileTextField().getText();
    String testCaseName = getTestCaseName();
    if (myTestMethodSettingsSection != null) {
      myTestMethodSettingsSection.stateChanged(project, jsTestFilePath, testCaseName);
    }
  }

  private void updateTestCaseVariants(@NotNull Project project, @NotNull String jsTestFilePath) {
    myTestCaseNameComboBox.removeAllItems();
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
      for (Object topLevel : pack.getTopLevelElements()) {
        myTestCaseNameComboBox.addItem(topLevel);
      }
    }
  }

  private String getTestCaseName() {
    Object value = myTestCaseNameComboBox.getSelectedItem();
    return value == null ? "" : value.toString();
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
    myJsFileRunSettingsSection.resetFrom(runSettings);
    myTestCaseNameComboBox.setSelectedItem(runSettings.getTestCaseName());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myJsFileRunSettingsSection.applyTo(runSettingsBuilder);
    String testCaseName = getTestCaseName();
    runSettingsBuilder.setTestCaseName(testCaseName);
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
        0, 0,
        2, 1,
        1.0, 0.0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
      );
      JComponent jsFileComponent = myJsFileRunSettingsSection.getComponent(creationContext);
      panel.add(jsFileComponent, c);
    }
    {
      myLabel.setDisplayedMnemonic('e');
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      GridBagConstraints c = new GridBagConstraints(
        0, 1,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.EAST,
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
      panel.add(myTestCaseNameComboBox, c);
      myLabel.setLabelFor(myTestCaseNameComboBox);
    }

    SwingUtils.addGreedyBottomRow(panel);
    trackJsTestFilePathChanges(creationContext.getProject());

    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
    myJsFileRunSettingsSection.setAnchor(anchor);
  }

}
