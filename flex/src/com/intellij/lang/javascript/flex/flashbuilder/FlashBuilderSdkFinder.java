package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class FlashBuilderSdkFinder {

  private boolean myInitialized = false;

  private final Project myProject;
  private final String myInitiallySelectedDirPath;
  private final Collection<FlashBuilderProject> myFlashBuilderProjects;

  private String myWorkspacePath;
  private WhereToLookForSdkInfo myWhereToLookForSdkInfo;
  private Map<String, String> mySdkNameToRootPath = new HashMap<String, String>();
  private Collection<VirtualFile> mySdkRootsFromFBInstallation = new ArrayList<VirtualFile>();
  private Sdk mySdk;

  private static final String FLEX_SDK_PROPERTY = "com.adobe.flexbuilder.project.flex_sdks";
  private static final String SDKS_ELEMENT = "sdks";
  private static final String SDK_ELEMENT = "sdk";
  private static final String DEFAULT_SDK_ATTR = "defaultSDK";
  private static final String SDK_NAME_ATTR = "name";
  private static final String SDK_LOCATION_ATTR = "location";

  private static final String COMMON_SDK_NAME_BEGINNING = "Flex ";

  private enum WhereToLookForSdkInfo {
    InWorkspace, InFBInstallationDir, UseIdeaSdk
  }

  public FlashBuilderSdkFinder(final Project project,
                               final String initiallySelectedDirPath,
                               final Collection<FlashBuilderProject> flashBuilderProjects) {
    myProject = project;
    myInitiallySelectedDirPath = initiallySelectedDirPath;
    myFlashBuilderProjects = flashBuilderProjects;
  }

  @Nullable
  public String getWorkspacePath () {
    return  myInitialized ? myWorkspacePath : findWorkspacePath();
  }

  @Nullable
  public Sdk findSdk(final FlashBuilderProject flashBuilderProject) {
    if (!myInitialized) {
      initialize();
      myInitialized = true;
    }

    switch (myWhereToLookForSdkInfo) {
      case InWorkspace:
        final String sdkRootPath = mySdkNameToRootPath.get(flashBuilderProject.getSdkName());
        return createOrGetSdk(sdkRootPath, flashBuilderProject.getProjectType());
      case InFBInstallationDir:
        return findMostSuitableSdkInInstallation(flashBuilderProject);
      case UseIdeaSdk:
        return mySdk;
    }

    return null;
  }

  @Nullable
  private Sdk findMostSuitableSdkInInstallation(final FlashBuilderProject flashBuilderProject) {
    final String sdkName = flashBuilderProject.getSdkName();

    if ("".equals(sdkName) && !mySdkRootsFromFBInstallation.isEmpty()) {
      // sdk roots are usually called with version string, i.e. '3.4.1' or '4.0.0'
      VirtualFile latestSdkRoot = mySdkRootsFromFBInstallation.iterator().next();
      for (VirtualFile nextSdkRoot : mySdkRootsFromFBInstallation) {
        if (StringUtil.compareVersionNumbers(nextSdkRoot.getName(), latestSdkRoot.getName()) > 0) {
          latestSdkRoot = nextSdkRoot;
        }
      }
      return createOrGetSdk(latestSdkRoot.getPath(), flashBuilderProject.getProjectType());
    }

    if (sdkName.startsWith(COMMON_SDK_NAME_BEGINNING)) {
      final String sdkVersion = sdkName.substring(COMMON_SDK_NAME_BEGINNING.length());
      for (VirtualFile sdkRoot : mySdkRootsFromFBInstallation) {
        if (sdkRoot.getName().startsWith(sdkVersion)) {
          return createOrGetSdk(sdkRoot.getPath(), flashBuilderProject.getProjectType());
        }
      }
    }

    return mySdkRootsFromFBInstallation.isEmpty()
           ? null
           : createOrGetSdk(mySdkRootsFromFBInstallation.iterator().next().getPath(), flashBuilderProject.getProjectType());
  }

  private void initialize() {
    myWorkspacePath = findWorkspacePath();
    if (myWorkspacePath != null) {
      initializeForWorkspace(myWorkspacePath);
    }
    else {
      final FlashBuilderSdkDialog dialog = new FlashBuilderSdkDialog(myProject, true);
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        dialog.show();
      }
      else {
        dialog.close(DialogWrapper.OK_EXIT_CODE);
      }
      if (dialog.isOK()) {
        if (dialog.isUseIdeaSdkSelected()) {
          myWhereToLookForSdkInfo = WhereToLookForSdkInfo.UseIdeaSdk;
          mySdk = dialog.getSdk();
        }
        else {
          myWorkspacePath = dialog.getWorkspacePath();
          initializeForWorkspace(myWorkspacePath);
        }
      }
      else {
        myWhereToLookForSdkInfo = WhereToLookForSdkInfo.UseIdeaSdk;
        mySdk = null;
      }
    }
  }

  @Nullable
  private String findWorkspacePath() {
    String workspacePath = guessWorkspacePath(myInitiallySelectedDirPath);
    if (workspacePath == null) {
      for (FlashBuilderProject flashBuilderProject : myFlashBuilderProjects) {
        workspacePath = guessWorkspacePath(flashBuilderProject.getProjectRootPath());
        if (workspacePath != null) {
          break;
        }
      }
    }
    return workspacePath;
  }

  private void initializeForWorkspace(final String workspacePath) {
    if (!initializeSdksConfiguredInWorkspace(workspacePath)) {
      final FlashBuilderSdkDialog dialog = new FlashBuilderSdkDialog(myProject, false);
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        dialog.show();
      }
      else {
        dialog.close(DialogWrapper.OK_EXIT_CODE);
      }
      if (dialog.isOK()) {
        if (dialog.isUseIdeaSdkSelected()) {
          myWhereToLookForSdkInfo = WhereToLookForSdkInfo.UseIdeaSdk;
          mySdk = dialog.getSdk();
        }
        else {
          initializeForFBInstallationDir(dialog.getFBInstallationPath());
        }
      }
      else {
        myWhereToLookForSdkInfo = WhereToLookForSdkInfo.UseIdeaSdk;
        mySdk = null;
      }
    }
  }

  private void initializeForFBInstallationDir(final String fbInstallationPath) {
    myWhereToLookForSdkInfo = WhereToLookForSdkInfo.InFBInstallationDir;

    final VirtualFile sdksDir =
      LocalFileSystem.getInstance().findFileByPath(fbInstallationPath + FlashBuilderProjectFinder.SDKS_RELATIVE_PATH);
    if (sdksDir != null) {
      for (VirtualFile potentialSdkRoot : sdksDir.getChildren()) {
        if (FlexSdkUtils.isFlexOrAirSdkRoot(potentialSdkRoot)) {
          mySdkRootsFromFBInstallation.add(potentialSdkRoot);
        }
      }
    }
  }

  private boolean initializeSdksConfiguredInWorkspace(final String flashBuilderWorkspacePath) {
    final Document sdkInfoDocument = loadSdkInfoDocument(flashBuilderWorkspacePath);
    if (sdkInfoDocument == null) {
      return false;
    }

    final Element sdksElement = sdkInfoDocument.getRootElement();
    if (!sdksElement.getName().equals(SDKS_ELEMENT)) return false;

    //noinspection unchecked
    for (final Element sdkElement : ((Iterable<Element>)sdksElement.getChildren(SDK_ELEMENT))) {
      final Attribute defaultSdkAttr = sdkElement.getAttribute(DEFAULT_SDK_ATTR);
      final Attribute sdkNameAttr = sdkElement.getAttribute(SDK_NAME_ATTR);
      final Attribute sdkLocationAttr = sdkElement.getAttribute(SDK_LOCATION_ATTR);

      if (sdkLocationAttr != null) {
        if (defaultSdkAttr != null && defaultSdkAttr.getValue().equalsIgnoreCase("true")) {
          // empty string means default sdk
          mySdkNameToRootPath.put("", sdkLocationAttr.getValue());
        }

        if (sdkNameAttr != null) {
          mySdkNameToRootPath.put(sdkNameAttr.getValue(), sdkLocationAttr.getValue());
        }
      }
    }

    myWhereToLookForSdkInfo = WhereToLookForSdkInfo.InWorkspace;
    return true;
  }

  @Nullable
  private static Document loadSdkInfoDocument(final String flashBuilderWorkspacePath) {
    try {
      final VirtualFile projectPrefsFile =
        LocalFileSystem.getInstance().findFileByPath(flashBuilderWorkspacePath + FlashBuilderProjectFinder.PROJECT_PREFS_RELATIVE_PATH);
      if (projectPrefsFile == null) return null;

      final Properties projectPrefsProperties = new Properties();
      projectPrefsProperties.load(projectPrefsFile.getInputStream());
      final String xmlString = projectPrefsProperties.getProperty(FLEX_SDK_PROPERTY);
      if (xmlString == null) return null;

      return JDOMUtil.loadDocument(xmlString);
    }
    catch (IOException e) {/*ignore*/}
    catch (JDOMException e) {/*ignore*/}

    return null;
  }

  @Nullable
  private static Sdk createOrGetSdk(final String sdkHomePath, final FlashBuilderProject.ProjectType projectType) {
    //noinspection RedundantCast
    final SdkType sdkType =
      projectType == FlashBuilderProject.ProjectType.AIR ? (SdkType)AirSdkType.getInstance() : (SdkType)FlexSdkType.getInstance();
    return FlexSdkUtils.createOrGetSdk(sdkType, sdkHomePath);
  }

  @Nullable
  private static String guessWorkspacePath(final String selectedPath) {
    VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(selectedPath);
    while (dir != null) {
      if (FlashBuilderProjectFinder.isFlashBuilderWorkspace(dir)) {
        return dir.getPath();
      }
      dir = dir.getParent();
    }
    return null;
  }

}
