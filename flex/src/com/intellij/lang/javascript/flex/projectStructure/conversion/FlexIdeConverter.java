package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.*;
import com.intellij.conversion.impl.ModuleSettingsImpl;
import com.intellij.openapi.module.impl.ModuleManagerImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: ksafonov
 */
class FlexIdeConverter extends ProjectConverter {
  private ConversionParams myParams;
  private final ConversionContext myContext;

  FlexIdeConverter(ConversionContext context) {
    myContext = context;
  }

  @Override
  public boolean isConversionNeeded() {
    for (File file : myContext.getModuleFiles()) {
      try {
        ModuleSettings moduleSettings = myContext.getModuleSettings(file);
        if (FlexIdeModuleConverter.isConversionNeededStatic(moduleSettings)) return true;
      }
      catch (CannotConvertException ignored) {
      }
    }
    return false;
  }

  @Override
  public ConversionProcessor<ModuleSettings> createModuleFileConverter() {
    return new FlexIdeModuleConverter(getParams());
  }

  @Nullable
  public ConversionProcessor<WorkspaceSettings> createWorkspaceFileConverter() {
    return isConversionNeeded() ? new FlexWorkspaceConverter(getParams()) : null;
  }

  public ConversionParams getParams() {
    if (myParams == null) {
      myParams = new ConversionParams(myContext);
    }
    return myParams;
  }

  @Override
  public void preProcessingFinished() throws CannotConvertException {
    ComponentManagerSettings projectRootManagerSettings = myContext.getProjectRootManagerSettings();
    if (projectRootManagerSettings == null) return;
    Element projectRootManager = projectRootManagerSettings.getComponentElement(ProjectRootManager.class.getSimpleName());
    if (projectRootManager == null) return;

    getParams().projectSdkName = projectRootManager.getAttributeValue(ProjectRootManagerImpl.PROJECT_JDK_NAME_ATTR);
    getParams().projectSdkType = projectRootManager.getAttributeValue(ProjectRootManagerImpl.PROJECT_JDK_TYPE_ATTR);

    removeNonFlexModulesFromProject();
  }

  private void removeNonFlexModulesFromProject() throws CannotConvertException {
    ComponentManagerSettings modulesSettings = myContext.getModulesSettings();
    if (modulesSettings == null) return;
    final Element modulesManager = modulesSettings.getComponentElement(ModuleManagerImpl.COMPONENT_NAME);
    if (modulesManager == null) return;

    final Element modulesElement = modulesManager.getChild(ModuleManagerImpl.ELEMENT_MODULES);
    List<Element> modules = JDOMUtil.getChildren(modulesElement, ModuleManagerImpl.ELEMENT_MODULE);
    Collection<Element> modulesToRemove =
      ContainerUtil.findAll(modules, new Condition<Element>() {
        @Override
        public boolean value(Element module) {
          String filePath = module.getAttributeValue(ModuleManagerImpl.ATTRIBUTE_FILEPATH);
          ModuleSettings moduleSettings = myContext.getModuleSettings(ModuleSettingsImpl.getModuleName(new File(filePath)));
          return moduleSettings == null || !FlexIdeModuleConverter.isFlexModule(moduleSettings);
        }
      });
    if (!modulesToRemove.isEmpty()) {
      modules.removeAll(modulesToRemove);
    }
  }

  @Override
  public void postProcessingFinished() throws CannotConvertException {
    getParams().saveGlobalLibraries();
  }

  @Override
  public Collection<File> getAdditionalAffectedFiles() {
    return Collections.singleton(myContext.getModulesSettings().getFile());
  }
}
