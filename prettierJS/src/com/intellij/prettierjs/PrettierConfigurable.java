package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageField;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PrettierConfigurable implements SearchableConfigurable {
  @NotNull
  private final Project myProject;
  private final NodeJsInterpreterField myNodeInterpreterField;
  private final NodePackageField myPackageField;

  public PrettierConfigurable(@NotNull Project project) {
    myProject = project;
    myNodeInterpreterField = new NodeJsInterpreterField(myProject, false);
    myPackageField = new NodePackageField(myNodeInterpreterField, PrettierUtil.PACKAGE_NAME);
  }

  @NotNull
  @Override
  public String getId() {
    return "settings.javascript.prettier";
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Prettier";
  }

  public void showEditDialog() {
    ShowSettingsUtil.getInstance().editConfigurable(myProject, this);
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    JPanel mainPanel = FormBuilder.createFormBuilder()
                              .setAlignLabelOnRight(true)
                              .addLabeledComponent("&Node interpreter:", myNodeInterpreterField)
                              .addLabeledComponent("&Prettier package:", myPackageField)
                              .getPanel();
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(mainPanel, BorderLayout.NORTH);
    wrapper.setPreferredSize(new Dimension(400, 200));
    return wrapper;
  }

  @Override
  public boolean isModified() {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(myProject);
    return !configuration.getInterpreterRef().equals(myNodeInterpreterField.getInterpreterRef())
           || !myPackageField.getSelected().equals(configuration.getPackage());
  }

  @Override
  public void reset() {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(myProject);
    myNodeInterpreterField.setInterpreterRef(configuration.getInterpreterRef());
    myPackageField.setSelected(configuration.getPackage());
  }

  @Override
  public void apply() {
    NodePackage selectedPackage = myPackageField.getSelected();
    PrettierConfiguration.getInstance(myProject).update(myNodeInterpreterField.getInterpreterRef(), selectedPackage);
    PrettierLanguageService.getInstance(myProject).terminateStartedProcess(false);
  }

  public static class Provider extends ConfigurableProvider {

    private final Project myProject;

    public Provider(Project project) {
      myProject = project;
    }

    @Override
    public boolean canCreateConfigurable() {
      return true;
    }

    @Nullable
    @Override
    public Configurable createConfigurable() {
      return new PrettierConfigurable(myProject);
    }
  }
}
