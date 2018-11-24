package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdConfigType;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class OptionalConfigRunSettingsSection extends AbstractRunSettingsSection {

  private final JsFileRunSettingsSection myJsFileRunSettingsSection;
  private OneOfRunSettingsSection<ConfigTypeItem> myContentSection;
  private Map<JstdConfigType, ConfigTypeItem> myItemByConfigTypeMap;
  private ButtonGroup myButtonGroup;

  OptionalConfigRunSettingsSection(JsFileRunSettingsSection jsFileRunSettingsSection) {
    this.myJsFileRunSettingsSection = jsFileRunSettingsSection;
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    select(runSettings.getConfigType());
    myContentSection.resetFrom(runSettings);
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    String actionCommand = myButtonGroup.getSelection().getActionCommand();
    JstdConfigType configType = JstdConfigType.valueOf(actionCommand);
    select(configType);
    runSettingsBuilder.setConfigType(configType);
    myContentSection.applyTo(runSettingsBuilder);
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
          0, 0
      );
      JLabel txtLabel = new JLabel("JsTestDriver configuration file:");
      panel.add(txtLabel, c);
    }

    List<ConfigTypeItem> configTypeItems = createConfigTypeItemList();
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 1,
          configTypeItems.size() + 1, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
          0, 0
      );

      myContentSection = new OneOfRunSettingsSection<ConfigTypeItem>(configTypeItems);
      panel.add(myContentSection.getComponent(creationContext), c);

      myItemByConfigTypeMap = createItemByConfigTypeMap(configTypeItems);
    }

    myButtonGroup = new ButtonGroup();
    for (final ConfigTypeItem configTypeItem : configTypeItems) {
      JRadioButton radioButton = configTypeItem.getRadioButton();
      radioButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          select(configTypeItem.getConfigType());
        }
      });
      GridBagConstraints c = new GridBagConstraints(
          myButtonGroup.getButtonCount() + 1, 0,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.NONE,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
          0, 0
      );
      myButtonGroup.add(radioButton);
      panel.add(radioButton, c);
    }

    select(JstdConfigType.GENERATED);

    return panel;
  }

  private void select(JstdConfigType jstdConfigType) {
    ConfigTypeItem configTypeItem = myItemByConfigTypeMap.get(jstdConfigType);
    myContentSection.select(configTypeItem);

    ButtonModel buttonModel = configTypeItem.getRadioButton().getModel();
    if (!buttonModel.isSelected()) {
      myButtonGroup.setSelected(buttonModel, true);
    }
  }

  private List<ConfigTypeItem> createConfigTypeItemList() {
    return Arrays.asList(
        new ConfigTypeItem(JstdConfigType.GENERATED, "Generated", 'G') {
          @Override
          public RunSettingsSection provideRunSettingsSection() {
            return new AbstractRunSettingsSection() {
              @NotNull
              @Override
              protected JComponent createComponent(final @NotNull CreationContext creationContext) {
                return GeneratedConfigTypeComponent.INSTANCE.createComponent(creationContext.getProject(), myJsFileRunSettingsSection);
              }

              @Override
              public void resetFrom(@NotNull JstdRunSettings runSettings) {
              }

              @Override
              public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
              }
            };
          }
        },
        new ConfigTypeItem(JstdConfigType.FILE_PATH, "Custom", 'u') {
          @Override
          public RunSettingsSection provideRunSettingsSection() {
            return new AbstractRunSettingsSection() {

              private TextFieldWithBrowseButton myConfigFilePath;

              @NotNull
              @Override
              protected JComponent createComponent(@NotNull CreationContext creationContext) {

                myConfigFilePath = new TextFieldWithBrowseButton();
                myConfigFilePath.addBrowseFolderListener(
                    "Select JsTestDriver configuration file",
                    "",
                    creationContext.getProject(),
                    FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                );

                JPanel panel = new JPanel(new GridBagLayout());
                {
                  GridBagConstraints c = new GridBagConstraints(
                      0, 0,
                      1, 1,
                      1.0, 0.0,
                      GridBagConstraints.FIRST_LINE_START,
                      GridBagConstraints.HORIZONTAL,
                      new Insets(0, 0, 0, 0),
                      0, 0
                  );
                  panel.add(myConfigFilePath, c);
                }

                return panel;
              }

              @Override
              public void resetFrom(@NotNull JstdRunSettings runSettings) {
                myConfigFilePath.setText(runSettings.getConfigFile());
              }

              @Override
              public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
                runSettingsBuilder.setConfigFile(ObjectUtils.notNull(myConfigFilePath.getText(), ""));
              }
            };
          }
        }
    );
  }

  private Map<JstdConfigType, ConfigTypeItem> createItemByConfigTypeMap(List<ConfigTypeItem> configTypeItems) {
    Map<JstdConfigType, ConfigTypeItem> map = new HashMap<JstdConfigType, ConfigTypeItem>();
    for (ConfigTypeItem configTypeItem : configTypeItems) {
      map.put(configTypeItem.getConfigType(), configTypeItem);
    }
    return map;
  }

  private abstract class ConfigTypeItem implements IdProvider, RunSettingsSectionProvider {

    private final JstdConfigType myJstdConfigType;
    private final JRadioButton myRadioButton;

    private ConfigTypeItem(JstdConfigType jstdConfigType, String displayName, char mnemonic) {
      myJstdConfigType = jstdConfigType;
      myRadioButton = new JRadioButton(displayName);
      myRadioButton.setMnemonic(mnemonic);
      myRadioButton.setActionCommand(jstdConfigType.name());
    }

    @Override
    public String getId() {
      return myJstdConfigType.name();
    }

    public JstdConfigType getConfigType() {
      return myJstdConfigType;
    }

    public JRadioButton getRadioButton() {
      return myRadioButton;
    }
  }

}
