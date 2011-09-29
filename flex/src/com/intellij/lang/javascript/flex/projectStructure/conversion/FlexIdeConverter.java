package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.*;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import org.jdom.Element;

import java.io.File;

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

  public ConversionParams getParams() {
    if (myParams == null) {
      myParams = new ConversionParams();
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
  }

  @Override
  public void postProcessingFinished() throws CannotConvertException {
    getParams().saveGlobalLibraries();
  }
}
