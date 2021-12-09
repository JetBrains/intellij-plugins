// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.SelectFlexSdkDialog;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class FlashBuilderSdkFinder {

  static final String DEFAULT_SDK_NAME = ""; // used as default name in FB project files

  private boolean myInitialized = false;

  private final Project myProject;
  private final String myInitiallySelectedPath;
  private final List<? extends FlashBuilderProject> myAllProjects;

  private String myWorkspacePath;
  private final Map<String, String> mySdkNameToRootPath = new HashMap<>();
  private String myAirSdkHome;
  private Sdk mySdk;
  private boolean myDialogWasShown = false;

  private static final String FLEX_SDK_PROPERTY = "com.adobe.flexbuilder.project.flex_sdks";
  private static final String SDKS_ELEMENT = "sdks";
  public static final String SDKS_FOLDER = "sdks";
  private static final String SDK_ELEMENT = "sdk";
  private static final String DEFAULT_SDK_ATTR = "defaultSDK";
  private static final String SDK_NAME_ATTR = "name";
  private static final String SDK_LOCATION_ATTR = "location";

  public FlashBuilderSdkFinder(final Project project,
                               final String initiallySelectedPath,
                               final List<? extends FlashBuilderProject> allProjects) {
    myProject = project;
    myInitiallySelectedPath = initiallySelectedPath;
    myAllProjects = allProjects;
  }

  @Nullable
  public String getWorkspacePath() {
    return myInitialized ? myWorkspacePath : findWorkspacePath();
  }

  @Nullable
  private String findWorkspacePath() {
    final Collection<VirtualFile> checked = new HashSet<>();
    String wsPath = guessWorkspacePath(myInitiallySelectedPath, checked);
    if (wsPath == null) {
      for (FlashBuilderProject fbProject : myAllProjects) {
        wsPath = guessWorkspacePath(fbProject.getProjectRootPath(), checked);
        if (wsPath != null) return wsPath;
      }
    }
    return wsPath;
  }

  @Nullable
  private static String guessWorkspacePath(String path, final Collection<VirtualFile> checked) {
    VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(path);
    if (dir != null && !dir.isDirectory()) {
      dir = dir.getParent();
    }

    while (dir != null) {
      if (checked.contains(dir)) return null;
      if (FlashBuilderProjectFinder.isFlashBuilderWorkspace(dir)) return dir.getPath();
      dir = dir.getParent();
    }
    return null;
  }

  @Nullable
  public Sdk findSdk(final FlashBuilderProject fbProject) {
    if (!myInitialized) {
      initialize();
      myInitialized = true;
    }

    if (fbProject.isAirSdk() && myAirSdkHome != null) {
      return FlexSdkUtils.createOrGetSdk(FlexSdkType2.getInstance(), myAirSdkHome);
    }

    final String sdkHome = mySdkNameToRootPath.get(fbProject.getSdkName());
    if (sdkHome != null) return FlexSdkUtils.createOrGetSdk(FlexSdkType2.getInstance(), sdkHome);

    if (myDialogWasShown) return mySdk;

    final SelectFlexSdkDialog dialog = new SelectFlexSdkDialog(myProject,
                                                               FlexBundle.message("flash.builder.project.import.title"),
                                                               FlexBundle.message("sdk.for.imported.projects", myAllProjects.size()));
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      dialog.show();
    }
    else {
      dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
    }
    myDialogWasShown = true;
    mySdk = dialog.isOK() ? dialog.getSdk() : null;
    return mySdk;
  }

  private void initialize() {
    final String fbInstallationPath = findFBInstallationPath();

    if (fbInstallationPath != null && !ApplicationManager.getApplication().isUnitTestMode()) {
      initializeAirSdk(fbInstallationPath);

      // first look for SDKs in installation, then in workspace. Some can be overwritten by workspace-specific ones.
      initializeSdksFromFBInstallation(fbInstallationPath);
    }

    myWorkspacePath = findWorkspacePath();
    if (myWorkspacePath != null) {
      initSdksConfiguredInWorkspace(myWorkspacePath);
    }
  }

  private void initializeAirSdk(final @NotNull String fbInstallationPath) {
    final File pluginsDir = new File(fbInstallationPath + "/eclipse/plugins");
    if (!pluginsDir.isDirectory()) return;

    final File[] airSdkParents = pluginsDir.listFiles((dir, name) -> name.startsWith("com.adobe.flash.compiler_"));

    for (File airSdkParent : airSdkParents) {
      final String airSdkHome = airSdkParent.getPath() + "/AIRSDK";
      if (FlexSdkType2.getInstance().isValidSdkHome(airSdkHome)) {
        myAirSdkHome = airSdkHome;
        return;
      }
    }
  }

  private void initializeSdksFromFBInstallation(final @NotNull String fbInstallationPath) {
    final File sdksDir = new File(fbInstallationPath, SDKS_FOLDER);
    if (!sdksDir.isDirectory()) return;

    String maxVersion = "0";
    for (File sdkDir : sdksDir.listFiles()) {
      if (!sdkDir.isDirectory()) continue;
      final File descriptionFile = new File(sdkDir, "flex-sdk-description.xml");
      if (!descriptionFile.isFile()) return;

      final String nameElement = "<flex-sdk-description><name>";
      final String versionElement = "<flex-sdk-description><version>";
      FileInputStream is = null;
      try {
        is = new FileInputStream(descriptionFile);
        final Map<String, List<String>> info = FlexUtils.findXMLElements(is, Arrays.asList(nameElement, versionElement));

        final List<String> nameInfo = info.get(nameElement);
        if (nameInfo.isEmpty()) continue;

        mySdkNameToRootPath.put(nameInfo.get(0), FileUtil.toSystemIndependentName(sdkDir.getPath()));

        final List<String> versionInfo = info.get(versionElement);
        if (versionInfo.isEmpty()) continue;

        final String version = versionInfo.get(0);
        if (StringUtil.compareVersionNumbers(version, maxVersion) > 0) {
          maxVersion = version;
          mySdkNameToRootPath.put(DEFAULT_SDK_NAME, FileUtil.toSystemIndependentName(sdkDir.getPath()));
        }
      }
      catch (IOException ignore) {/**/}
      finally {
        if (is != null) {
          try {
            is.close();
          }
          catch (IOException ignore) {/**/}
        }
      }
    }
  }

  @Nullable
  public static String findFBInstallationPath() {
    final List<File> fbDirs = new ArrayList<>();

    final FileFilter filter = dir -> {
      final String name = dir.getName();
      return dir.isDirectory() && (name.contains("Flash") || name.contains("Flex")) && name.contains("Builder")
             && new File(dir, SDKS_FOLDER).isDirectory();
    };

    final String programsPath = SystemInfo.isMac ? "/Applications" : SystemInfo.isWindows ? System.getenv("ProgramFiles") : null;
    final File programsDir = programsPath == null ? null : new File(programsPath);

    if (programsDir != null && programsDir.isDirectory()) {
      Collections.addAll(fbDirs, programsDir.listFiles(filter));

      final File adobeDir = new File(programsDir, "Adobe");
      if (adobeDir.isDirectory()) {
        Collections.addAll(fbDirs, adobeDir.listFiles(filter));
      }
    }

    if (SystemInfo.isWindows) {
      final String programs64Path = System.getenv("ProgramW6432");
      final File programs64Dir = programs64Path == null ? null : new File(programs64Path);

      if (programs64Dir != null && programs64Dir.isDirectory()) {
        Collections.addAll(fbDirs, programs64Dir.listFiles(filter));

        final File adobeDir = new File(programs64Dir, "Adobe");
        if (adobeDir.isDirectory()) {
          Collections.addAll(fbDirs, adobeDir.listFiles(filter));
        }
      }
    }

    if (fbDirs.size() == 0) return null;
    if (fbDirs.size() == 1) return fbDirs.get(0).getPath();

    // check the most recent
    Pair<String, String> pathAndVersion = null;
    for (File fbDir : fbDirs) {
      final String version = guessFBVersion(fbDir.getName());
      if (pathAndVersion == null || StringUtil.compareVersionNumbers(version, pathAndVersion.second) > 0) {
        pathAndVersion = Pair.create(fbDir.getPath(), version);
      }
    }

    assert pathAndVersion != null;
    return pathAndVersion.first;
  }

  private static String guessFBVersion(final String fbInstallFolderName) {
    final String suffix = " (64 Bit)";
    final String folderName = fbInstallFolderName.endsWith(suffix)
                              ? fbInstallFolderName.substring(0, fbInstallFolderName.length() - suffix.length())
                              : fbInstallFolderName;
    final StringBuilder b = new StringBuilder();
    for (int i = folderName.length() - 1; i >= 0; i--) {
      final char ch = folderName.charAt(i);
      if ('.' == ch || Character.isDigit(ch)) {
        b.insert(0, ch);
      }
      else {
        break;
      }
    }
    return b.toString();
  }

  private boolean initSdksConfiguredInWorkspace(final String fbWorkspacePath) {
    final Element sdkInfoDocument = loadSdkInfoDocument(fbWorkspacePath);
    if (sdkInfoDocument == null) {
      return false;
    }

    final Element sdksElement = sdkInfoDocument;
    if (!sdksElement.getName().equals(SDKS_ELEMENT)) return false;

    for (final Element sdkElement : sdksElement.getChildren(SDK_ELEMENT)) {
      final Attribute defaultSdkAttr = sdkElement.getAttribute(DEFAULT_SDK_ATTR);
      final Attribute sdkNameAttr = sdkElement.getAttribute(SDK_NAME_ATTR);
      final Attribute sdkLocationAttr = sdkElement.getAttribute(SDK_LOCATION_ATTR);

      if (sdkLocationAttr != null) {
        if (defaultSdkAttr != null && defaultSdkAttr.getValue().equalsIgnoreCase("true")) {
          mySdkNameToRootPath.put(DEFAULT_SDK_NAME, FileUtil.toSystemIndependentName(sdkLocationAttr.getValue()));
        }

        if (sdkNameAttr != null) {
          mySdkNameToRootPath.put(sdkNameAttr.getValue(), FileUtil.toSystemIndependentName(sdkLocationAttr.getValue()));
        }
      }
    }

    return true;
  }

  @Nullable
  private static Element loadSdkInfoDocument(final String flashBuilderWorkspacePath) {
    try {
      final VirtualFile projectPrefsFile =
        LocalFileSystem.getInstance().findFileByPath(flashBuilderWorkspacePath + FlashBuilderProjectFinder.PROJECT_PREFS_RELATIVE_PATH);
      if (projectPrefsFile == null) return null;

      final Properties projectPrefsProperties = new Properties();
      projectPrefsProperties.load(projectPrefsFile.getInputStream());
      final String xmlString = projectPrefsProperties.getProperty(FLEX_SDK_PROPERTY);
      if (xmlString == null) return null;

      return JDOMUtil.load(xmlString);
    }
    catch (IOException e) {/*ignore*/}
    catch (JDOMException e) {/*ignore*/}

    return null;
  }
}
