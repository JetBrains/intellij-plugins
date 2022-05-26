package jetbrains.plugins.yeoman.projectGenerator.builder;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGeneratorOwnerPanel;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGeneratorPanel;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGeneratorWelcomePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class YeomanSelectGeneratorWizardStep extends ModuleWizardStep implements Disposable {
  private final YeomanModuleBuilder myBuilder;
  private YeomanProjectGeneratorWelcomePanel myPanel;
  private JPanel myOwnerPanel;

  public YeomanSelectGeneratorWizardStep(YeomanModuleBuilder builder, WizardContext context) {
    super();
    myBuilder = builder;
  }

  @Override
  public JComponent getComponent() {
    if (myOwnerPanel == null) {
      myOwnerPanel = new JPanel(new BorderLayout());
      final JScrollPane scrollPane = YeomanProjectGeneratorPanel.createScrollPane();
      myOwnerPanel.add(scrollPane, BorderLayout.CENTER);

      myPanel = new YeomanProjectGeneratorWelcomePanel(createOwnerPanel(scrollPane, myOwnerPanel), myBuilder.getSettings());
      myPanel.render();
    }
    return myOwnerPanel;
  }


  @Override
  public void updateDataModel() {
    if (myPanel != null) {
      myPanel.commitSettings();
    }
  }

  @Override
  public void dispose() {

  }

  @Override
  public boolean validate() throws ConfigurationException {
    if (myPanel != null) {
      final String validate = myPanel.validate();
      if (validate != null) {
        throw new ConfigurationException(validate);
      }
    }

    return super.validate();
  }

  public static YeomanProjectGeneratorOwnerPanel createOwnerPanel(final JScrollPane scrollPane, final JPanel ownerPanel) {
    return new YeomanProjectGeneratorOwnerPanel() {

      @Nullable
      @Override
      public ValidateHandler getValidateHandler() {
        return null;
      }

      @Override
      public void setCentralComponent(@NotNull JComponent component) {
        scrollPane.setViewportView(component);
      }

      @Override
      public void setBottomComponent(JComponent component) {
        ownerPanel.add(component, BorderLayout.SOUTH);
      }

      @Override
      public void setMainButtonEnable(boolean isEnable) {
        //not implemented for IDEA
      }

      @NotNull
      @Override
      public JPanel getMainPanel() {
        return ownerPanel;
      }

      @Override
      public void setMainButtonName(@NotNull String newName) {
        //not implemented for IDEA
      }

      @NotNull
      @Override
      public String getLocationTitle() {
        return "yeoman-generated-project";
      }

      @Nullable
      @Override
      public LabeledComponent<TextFieldWithBrowseButton> getLocationComponent() {
        return null;
      }

      @Override
      public void close(ActionEvent e) {
        //do nothing
      }
    };
  }


}
