package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class JstdRunConfigurationEditor extends SettingsEditor<JstdRunConfiguration> {

  private RootSection myRootSection;
  private JComponent myRootComponent;

  public JstdRunConfigurationEditor(Project project) {
    myRootSection = new RootSection();
    myRootComponent = myRootSection.getComponent(new CreationContext(project));
  }

  @Override
  protected void resetEditorFrom(JstdRunConfiguration runConfiguration) {
    JstdRunSettings runSettings = runConfiguration.getRunSettings();
    myRootSection.resetFrom(runSettings);
  }

  @Override
  protected void applyEditorTo(JstdRunConfiguration runConfiguration) throws ConfigurationException {
    JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
    myRootSection.applyTo(builder);
    runConfiguration.setRunSettings(builder.build());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myRootComponent;
  }

  @Override
  protected void disposeEditor() {
  }

  private static class RootSection extends AbstractRunSettingsSection {

    private JComboBox myTestTypeComboBox;
    private OneOfRunSettingsSection<TestTypeListItem> myTestTypeContentRunSettingsSection;
    private ServerConfigurationForm myServerConfigurationForm = new ServerConfigurationForm();
    private Map<TestType, TestTypeListItem> myListItemByTestTypeMap;
    private final JBLabel myLabel = new JBLabel("Test:");

    @NotNull
    @Override
    protected JComponent createComponent(@NotNull CreationContext creationContext) {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.add(myLabel, new GridBagConstraints(
        0, 0,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(0, 0, 0, UIUtil.DEFAULT_HGAP),
        0, 0
      ));

      List<TestTypeListItem> testTypeListItems = createTestTypeListItems();

      myTestTypeComboBox = createTestTypeComboBox(testTypeListItems);
      panel.add(myTestTypeComboBox, new GridBagConstraints(
        1, 0,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(0, 0, 0, 0),
        0, 0
      ));
      myListItemByTestTypeMap = createListItemByTestTypeMap(testTypeListItems);

      myTestTypeContentRunSettingsSection = new OneOfRunSettingsSection<TestTypeListItem>(testTypeListItems);
      panel.add(myTestTypeContentRunSettingsSection.getComponent(creationContext), new GridBagConstraints(
        0, 1,
        2, 1,
        1.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
      ));

      panel.add(myServerConfigurationForm.getComponent(creationContext), new GridBagConstraints(
        0, 2,
        2, 1,
        1.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
      ));

      setAnchor(myTestTypeContentRunSettingsSection.getAnchor());
      return panel;
    }

    @Override
    public void resetFrom(@NotNull JstdRunSettings runSettings) {
      selectTestType(runSettings.getTestType());
      myTestTypeContentRunSettingsSection.resetFrom(runSettings);
      myServerConfigurationForm.resetFrom(runSettings);
    }

    @Override
    public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
      TestType selectedTestType = getSelectedTestType();
      selectTestType(selectedTestType);
      runSettingsBuilder.setTestType(selectedTestType);
      myTestTypeContentRunSettingsSection.applyTo(runSettingsBuilder);
      myServerConfigurationForm.applyTo(runSettingsBuilder);
    }

    private static @NotNull List<TestTypeListItem> createTestTypeListItems() {
      return Arrays.asList(
        new TestTypeListItem(TestType.ALL_CONFIGS_IN_DIRECTORY, "All configuration files in directory",
                             new AllInDirectoryRunSettingsSection()),
        new TestTypeListItem(TestType.CONFIG_FILE, "Configuration file", new ConfigFileRunSettingsSection()),
        new TestTypeListItem(TestType.JS_FILE, "JavaScript test file", new JsFileRunSettingsSection()),
        new TestTypeListItem(TestType.TEST_CASE, "Case", new TestCaseRunSettingsSection()),
        new TestTypeListItem(TestType.TEST_METHOD, "Method", new TestMethodRunSettingsSection())
      );
    }

    private static @NotNull Map<TestType, TestTypeListItem> createListItemByTestTypeMap(
      @NotNull List<TestTypeListItem> testTypeListItems
    ) {
      EnumMap<TestType, TestTypeListItem> map = new EnumMap<TestType, TestTypeListItem>(TestType.class);
      for (TestTypeListItem testTypeListItem : testTypeListItems) {
        map.put(testTypeListItem.getTestType(), testTypeListItem);
      }
      return map;
    }

    @NotNull
    private JComboBox createTestTypeComboBox(@NotNull List<TestTypeListItem> testTypeListItems) {
      JComboBox comboBox = new JComboBox(testTypeListItems.toArray());
      comboBox.setRenderer(new ListCellRendererWrapper<TestTypeListItem>(comboBox.getRenderer()) {
        @Override
        public void customize(JList list, TestTypeListItem value, int index, boolean selected, boolean hasFocus) {
          setText(value.getDisplayName());
        }
      });
      comboBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          selectTestType(getSelectedTestType());
        }
      });
      return comboBox;
    }

    private void selectTestType(TestType testType) {
      TestTypeListItem testTypeListItem = myListItemByTestTypeMap.get(testType);
      ComboBoxModel comboBoxModel = myTestTypeComboBox.getModel();
      if (comboBoxModel.getSelectedItem() != testTypeListItem) {
        comboBoxModel.setSelectedItem(testTypeListItem);
      }
      myTestTypeContentRunSettingsSection.select(testTypeListItem);
    }

    private @NotNull TestType getSelectedTestType() {
      return ((TestTypeListItem) myTestTypeComboBox.getSelectedItem()).getTestType();
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
      super.setAnchor(anchor);
      myTestTypeContentRunSettingsSection.setAnchor(anchor);
      myLabel.setAnchor(anchor);
    }
  }
}
