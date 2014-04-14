package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Masahiro Suzuka on 2014/04/13.
 */
public class PhoneGapModuleBuilder extends ModuleBuilder implements ModuleBuilderListener {

  public PhoneGapModuleBuilder() {
    addListener(this);
  }

  @Override
  public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    System.out.println("setupRootModel");
    String path = modifiableRootModel.getProject().getBasePath();
    System.out.println(path);
    modifiableRootModel.addContentEntry("file://" + path);
  }

  @Override
  public void moduleCreated(@NotNull Module module) {
    final String workingDir = module.getProject().getBasePath();
    final String projectName = module.getProject().getName();
    GeneralCommandLine runPhoneGapCommandLine = new GeneralCommandLine("phonegap",
                                                            "create",
                                                            "temp",
                                                            "-n",
                                                            projectName);
    runPhoneGapCommandLine.setWorkDirectory(workingDir);
    try {
      OSProcessHandler handler = new OSProcessHandler(runPhoneGapCommandLine);
      handler.addProcessListener(new PhoneGapCommandLineListener(module.getProject()));
      handler.startNotify();
      runPhoneGapCommandLine.createProcess();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {}
  }

  @Override
  public ModuleType getModuleType() {
    return PhoneGapModuleType.getInstance();
  }
}
