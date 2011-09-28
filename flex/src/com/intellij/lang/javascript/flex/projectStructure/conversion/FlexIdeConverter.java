package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.*;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.ProjectJdkTableImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectRootManagerImpl;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * User: ksafonov
 */
class FlexIdeConverter extends ProjectConverter {
  private static final Logger LOG = Logger.getInstance(FlexIdeConverter.class.getName());
  private static final String COMPONENT_NAME = "libraryTable";

  private final ConversionParams myParams = new ConversionParams();
  private final ConversionContext myContext;

  private static final String[] SDK_TYPES = new String[]{FlexSdkType.NAME, AirSdkType.NAME, AirMobileSdkType.NAME};

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
    return new FlexIdeModuleConverter(myParams);
  }

  @Override
  public void preProcessingFinished() throws CannotConvertException {
    ComponentManagerSettings projectRootManagerSettings = myContext.getProjectRootManagerSettings();
    if (projectRootManagerSettings == null) return;
    Element projectRootManager = projectRootManagerSettings.getComponentElement(ProjectRootManager.class.getSimpleName());
    if (projectRootManager == null) return;

    myParams.projectSdkName = projectRootManager.getAttributeValue(ProjectRootManagerImpl.PROJECT_JDK_NAME_ATTR);
    myParams.projectSdkType = projectRootManager.getAttributeValue(ProjectRootManagerImpl.PROJECT_JDK_TYPE_ATTR);

    File jdkConfigFile = PathManager.getOptionsFile("jdk.table");
    if (jdkConfigFile.isFile()) {
      try {
        Document document = JDOMUtil.loadDocument(jdkConfigFile);
        Element jdkTable = JDomConvertingUtil.findComponent(document.getRootElement(), "ProjectJdkTable");
        if (jdkTable != null) {
          for (Object o : jdkTable.getChildren(ProjectJdkTableImpl.ELEMENT_JDK)) {
            Element jdk = (Element)o;
            if (!"2".equals(jdk.getAttributeValue(ProjectJdkImpl.ELEMENT_VERSION))) continue;
            Element typeElement = jdk.getChild(ProjectJdkImpl.ELEMENT_TYPE);
            if (typeElement == null) continue;
            String type = typeElement.getAttributeValue(ProjectJdkImpl.ATTRIBUTE_VALUE);
            if (!ArrayUtil.contains(type, SDK_TYPES)) continue;
            Element nameElement = jdk.getChild(ProjectJdkImpl.ELEMENT_NAME);
            if (nameElement == null) continue;
            String name = nameElement.getAttributeValue(ProjectJdkImpl.ATTRIBUTE_VALUE);
            if (StringUtil.isEmpty(name)) continue;
            Element homeElement = jdk.getChild(ProjectJdkImpl.ELEMENT_HOMEPATH);
            if (homeElement == null) continue;
            String homePath = homeElement.getAttributeValue(ProjectJdkImpl.ATTRIBUTE_VALUE);
            if (StringUtil.isEmpty(homePath)) {
              continue;
            }
            myParams.addIdeaSdk(name, homePath);
          }
        }
      }
      catch (JDOMException e) {
        LOG.warn(e);
      }
      catch (IOException e) {
        LOG.warn(e);
      }
    }

    File libConfigFile = PathManager.getOptionsFile(ApplicationLibraryTable.getExternalFileName());
    if (libConfigFile.isFile()) {
      try {
        Document document = JDOMUtil.loadDocument(libConfigFile);
        Element libraryTable = JDomConvertingUtil.findComponent(document.getRootElement(), COMPONENT_NAME);
        if (libraryTable != null) {
          for (Object o : libraryTable.getChildren(LibraryImpl.ELEMENT)) {
            Element library = (Element)o;
            String name = library.getAttributeValue(LibraryImpl.LIBRARY_NAME_ATTR);
            myParams.addExistingGlobalLibraryName(name);

            String type = library.getAttributeValue(LibraryImpl.LIBRARY_TYPE_ATTR);
            if (!FlexSdkLibraryType.FLEX_SDK.getKindId().equals(type)) {
              continue;
            }
            Element propertiesElement = library.getChild(LibraryImpl.PROPERTIES_ELEMENT);
            FlexSdkProperties properties = XmlSerializer.deserialize(propertiesElement, FlexSdkProperties.class);
            myParams.addExistingFlexIdeSdk(properties.getHomePath(), properties.getId());
          }
        }
      }
      catch (JDOMException e) {
        LOG.warn(e);
      }
      catch (IOException e) {
        LOG.warn(e);
      }
    }
  }

  @Override
  public void postProcessingFinished() throws CannotConvertException {
    Map<String, String> sdks = myParams.getFlexIdeSdksToCreate();
    if (sdks.isEmpty()) return;

    File libConfigFile = PathManager.getOptionsFile(ApplicationLibraryTable.getExternalFileName());
    try {
      Document document;
      if (libConfigFile.isFile()) {
        document = JDOMUtil.loadDocument(libConfigFile);
      }
      else {
        Element element = new Element("application");
        document = new Document(element);
      }

      Element libraryTable = JDomConvertingUtil.findOrCreateComponentElement(document.getRootElement(), COMPONENT_NAME);
      final PathMacroManager pathMacroManager = PathMacroManager.getInstance(ApplicationManager.getApplication());

      for (Map.Entry<String, String> entry : sdks.entrySet()) {
        final String homePath = pathMacroManager.expandPath(entry.getKey());
        final VirtualFile sdkHome = LocalFileSystem.getInstance().findFileByPath(homePath);
        ConverterFlexSdkModificator sdkModificator =
          new ConverterFlexSdkModificator(homePath, entry.getValue(), myParams.getExistingGlobalLibrariesNames());
        FlexSdkUtils.setupSdkPaths(sdkHome, null, sdkModificator);
        if (sdkModificator.getLibraryElement() != null) {
          libraryTable.addContent(sdkModificator.getLibraryElement());
        }
        myParams.addExistingGlobalLibraryName(sdkModificator.getName());
      }

      JDOMUtil.writeDocument(document, libConfigFile, "\n");
    }
    catch (JDOMException e) {
      LOG.warn(e);
    }
    catch (IOException e) {
      LOG.warn(e);
    }
  }
}
