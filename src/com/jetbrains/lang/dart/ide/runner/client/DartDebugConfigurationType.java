package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartDebugConfigurationType implements ConfigurationType {
  private final ConfigurationFactory[] myFactories;
  private final LocalDartDebugConfigurationFactory myLocalFactory;
  private final RemoteDartDebugConfigurationFactory myRemoteFactory;

  public DartDebugConfigurationType() {
    myLocalFactory = new LocalDartDebugConfigurationFactory(this);
    myRemoteFactory = new RemoteDartDebugConfigurationFactory(this);
    myFactories = new ConfigurationFactory[]{myLocalFactory, myRemoteFactory};
  }

  public ConfigurationFactory getRemoteFactory() {
    return myRemoteFactory;
  }

  public ConfigurationFactory getLocalFactory() {
    return myLocalFactory;
  }


  public String getDisplayName() {
    return DartBundle.message("dart.configuration.name");
  }

  public String getConfigurationTypeDescription() {
    return DartBundle.message("dart.configuration.description");
  }

  public Icon getIcon() {
    return icons.DartIcons.Dart_16;
  }

  @NotNull
  public String getId() {
    return "DartDebugSession";
  }

  public ConfigurationFactory[] getConfigurationFactories() {
    return myFactories;
  }

  @NotNull
  public static DartDebugConfigurationType getTypeInstance() {
    final DartDebugConfigurationType type =
      ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), DartDebugConfigurationType.class);
    assert type != null;
    return type;
  }

  private static abstract class DartDebugConfigurationFactory extends ConfigurationFactory {
    private final String myName;

    public DartDebugConfigurationFactory(@NotNull final ConfigurationType type, @NotNull String name) {
      super(type);
      myName = name;
    }

    public String getName() {
      return myName;
    }
  }

  private static class LocalDartDebugConfigurationFactory extends DartDebugConfigurationFactory {
    public LocalDartDebugConfigurationFactory(@NotNull final ConfigurationType type) {
      super(type, DartBundle.message("dart.debug.configuration.local"));
    }

    public RunConfiguration createTemplateConfiguration(final Project project) {
      return new LocalDartDebugConfiguration(project, this, "");
    }
  }

  private static class RemoteDartDebugConfigurationFactory extends DartDebugConfigurationFactory {
    public RemoteDartDebugConfigurationFactory(@NotNull final ConfigurationType type) {
      super(type, DartBundle.message("dart.debug.configuration.remote"));
    }

    public RunConfiguration createTemplateConfiguration(final Project project) {
      return new RemoteDartDebugConfiguration(project, this, "");
    }
  }
}
