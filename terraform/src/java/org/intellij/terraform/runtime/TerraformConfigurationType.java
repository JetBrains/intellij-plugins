// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.intellij.terraform.TerraformIcons;
import org.intellij.terraform.hcl.HCLBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TerraformConfigurationType implements ConfigurationType {
  private final ConfigurationFactory myBaseFactory;
  private final ConfigurationFactory myPlanFactory;
  private final ConfigurationFactory myApplyFactory;

  public TerraformConfigurationType() {
    myBaseFactory = new MyConfigurationFactory(null, null, "Terraform");
    myPlanFactory = new MyConfigurationFactory(HCLBundle.message("terraform.run.configuration.type.plan.name.suffix"), "plan", "Terraform Plan");
    myApplyFactory = new MyConfigurationFactory(HCLBundle.message("terraform.run.configuration.type.apply.name.suffix"), "apply", "Terraform Apply");
  }

  @NotNull
  public static TerraformConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(TerraformConfigurationType.class);
  }

  public ConfigurationFactory getBaseFactory() {
    return myBaseFactory;
  }

  public ConfigurationFactory getPlanFactory() {
    return myPlanFactory;
  }

  public ConfigurationFactory getApplyFactory() {
    return myApplyFactory;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return HCLBundle.message("terraform.configuration.title");
  }

  @Override
  public String getConfigurationTypeDescription() {
    return HCLBundle.message("terraform.configuration.type.description");
  }

  @Override
  public Icon getIcon() {
    return TerraformIcons.Terraform;
  }

  @Override
  public ConfigurationFactory[] getConfigurationFactories() {
    return new ConfigurationFactory[]{myBaseFactory, myPlanFactory, myApplyFactory};
  }

  @NotNull
  @Override
  public String getId() {
    return "#org.intellij.plugins.hcl.terraform.run.TerraformConfigurationType";
  }

  private class MyConfigurationFactory extends ConfigurationFactory {
    private final String myParameters;
    @Nls private final String myNameSuffix;
    private final String myId;

    public MyConfigurationFactory(@Nls @Nullable String nameSuffix, String parameters, String id) {
      super(TerraformConfigurationType.this);
      myNameSuffix = nameSuffix;
      myParameters = parameters;
      myId = id;
    }

    @NotNull
    @Nls
    @Override
    public String getName() {
      final String name = super.getName();
      if (myNameSuffix != null) return name + " " + myNameSuffix;
      return name;
    }

    @Override
    public @NotNull String getId() {
      return myId;
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
      TerraformRunConfiguration configuration = new TerraformRunConfiguration(project, this, "");
      String path = project.getBasePath();
      if (path != null) {
        configuration.setWorkingDirectory(path);
      }
      if (myParameters != null) {
        configuration.setProgramParameters(myParameters);
      }
      return configuration;
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
      // TODO: Implement
      return true;
    }
  }
}
