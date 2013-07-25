package com.intellij.coldFusion.UI.runner;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlRunConfigurationFactory extends ConfigurationFactory {

    public CfmlRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    public RunConfiguration createTemplateConfiguration(Project project) {
        return new CfmlRunConfiguration(project, this, "Cold Fusion");
    }


}
