package com.jetbrains.plugins.meteor.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.jetbrains.plugins.meteor.MeteorBundle;
import icons.MeteorIcons;
import org.jetbrains.annotations.NotNull;

public final class MeteorConfigurationType extends ConfigurationTypeBase implements DumbAware {
  private MeteorConfigurationType() {
    super("meteor-app-runner", 
          MeteorBundle.message("meteor.name"), 
          MeteorBundle.message("meteor.run.description"), MeteorIcons.Meteor2);
    addFactory(new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new MeteorRunConfiguration(project, this, "Meteor");
      }

      @Override
      public @NotNull String getId() {
        return "Meteor";
      }
    });
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.meteor-app-runner";
  }
}
