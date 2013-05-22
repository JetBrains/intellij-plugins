package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.module.DartModuleType;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author: Fedor.Korotkov
 */
public class DartCommandLineRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
  implements RunConfigurationWithSuppressedDefaultRunAction {
  private final DartCommandLineRunConfigurationType myConfigurationType;
  @Nullable
  private String myFilePath = null;
  @Nullable
  private String myVMOptions = null;
  @Nullable
  private String myArguments = null;

  public DartCommandLineRunConfiguration(String name, Project project, DartCommandLineRunConfigurationType configurationType) {
    super(name, new RunConfigurationModule(project), configurationType.getConfigurationFactories()[0]);
    myConfigurationType = configurationType;
  }

  @Nullable
  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(@Nullable String fileUrl) {
    myFilePath = fileUrl;
  }

  @Nullable
  public String getVMOptions() {
    return myVMOptions;
  }

  public void setVMOptions(@Nullable String vmOptions) {
    myVMOptions = vmOptions;
  }

  @Nullable
  public String getArguments() {
    return myArguments;
  }

  public void setArguments(@Nullable String arguments) {
    myArguments = arguments;
  }

  @Override
  public Collection<Module> getValidModules() {
    Module[] modules = ModuleManager.getInstance(getProject()).getModules();
    return ContainerUtil.filter(modules, new Condition<Module>() {
      @Override
      public boolean value(Module module) {
        ModuleType moduleType = ModuleType.get(module);
        return moduleType == DartModuleType.getInstance() || moduleType instanceof WebModuleTypeBase;
      }
    });
  }

  @Override
  protected ModuleBasedConfiguration createInstance() {
    return new DartCommandLineRunConfiguration(getName(), getProject(), myConfigurationType);
  }

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DartCommandLineConfigurationEditorForm(getProject());
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
    final RunConfigurationModule configurationModule = getConfigurationModule();
    final Module module = configurationModule.getModule();
    if (module == null) {
      throw new RuntimeConfigurationException(DartBundle.message("dart.run.no.module", getName()));
    }
  }

  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    return DartCommandLineRunner.EMPTY_RUN_STATE;
  }


  public void writeExternal(final Element element) throws WriteExternalException {
    super.writeExternal(element);
    writeModule(element);
    XmlSerializer.serializeInto(this, element);
  }

  public void readExternal(final Element element) throws InvalidDataException {
    super.readExternal(element);
    readModule(element);
    XmlSerializer.deserializeInto(this, element);
  }
}
