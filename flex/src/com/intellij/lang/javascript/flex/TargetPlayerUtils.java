package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TargetPlayerUtils {

  private TargetPlayerUtils() {
  }

  /**
   * @return <code>false</code> for old Flex SDKs that do not support &lt;target-player&gt; tag in compiler configuration file
   */
  public static boolean isTargetPlayerApplicable(final @NotNull Sdk flexSdk) {
    return !FlexSdkUtils.isFlex2Sdk(flexSdk);
  }

  /**
   * Checks if the value of <code>targetPlayerVersion</code> parameter can be used in &lt;target-player&gt; tag of compiler configuration file.
   */
  public static boolean isTargetPlayerValid(final @NotNull String targetPlayerVersion) {
    final String[] versionParts = targetPlayerVersion.split("[.]");
    if (versionParts.length == 0 || versionParts[0].length() == 0) {
      return false;
    }
    for (int i = 0; i < versionParts.length; i++) {
      final String versionPart = versionParts[i];
      if (versionPart.length() > 0) {
        try {
          Integer.parseInt(versionPart);
        }
        catch (NumberFormatException e) {
          return false;
        }
      }
      else if (versionParts.length > i + 1 && versionParts[i + 1].length() > 0) {
        return false;
      }
    }
    return true;
  }

  public static String getTargetPlayerVersion(final Sdk sdk) {
    if (sdk == null) {
      return "";
    }
    final String playerglobalSwcVersion = getPlayerglobalSwcVersion(sdk);
    final String versionFromConfigXml = getTargetPlayerVersionFromConfigXmlFile(sdk);
    if (playerglobalSwcVersion == null) {
      return versionFromConfigXml == null ? "" : versionFromConfigXml;
    }
    else {
      if (versionFromConfigXml == null) {
        return playerglobalSwcVersion;
      }
      else {
        if (majorAndMinorVersionEqual(playerglobalSwcVersion, versionFromConfigXml)) {
          return versionFromConfigXml;
        }
        else {
          return playerglobalSwcVersion;
        }
      }
    }
  }

  public static boolean majorAndMinorVersionEqual(final String playerVersion1, final String playerVersion2) {
    return getPlayerMajorMinorVersion(playerVersion1).equals(getPlayerMajorMinorVersion(playerVersion2));
  }

  @Nullable
  private static String getPlayerglobalSwcVersion(final Sdk flexSdk) {
    for (final String rootUrl : flexSdk.getRootProvider().getUrls(OrderRootType.CLASSES)) {
      if (rootUrl.endsWith("/playerglobal.swc!/")) {
        final String temp = rootUrl.substring(0, rootUrl.length() - "/playerglobal.swc!/".length());
        final String playerVersion = temp.substring(temp.lastIndexOf("/") + 1);
        final String flexSdkVersion = flexSdk.getVersionString();
        return (flexSdkVersion != null && flexSdkVersion.startsWith("3.0"))
               ? "9"
               : playerVersion.matches("[0-9]+(\\.[0-9]+)?")
                 ? playerVersion
                 : null;
      }
    }
    return null;
  }

  @Nullable
  private static String getTargetPlayerVersionFromConfigXmlFile(final Sdk sdk) {
    return getTargetPlayerFromConfigXmlFile(sdk.getHomeDirectory(), sdk.getSdkType());
  }

  @Nullable
  public static String getTargetPlayerFromConfigXmlFile(final VirtualFile sdkRoot, final SdkType sdkType) {
    if (sdkRoot == null || !sdkRoot.isValid()) {
      return null;
    }
    final String configFileRelativePath = sdkType instanceof AirSdkType ? "frameworks/air-config.xml" : "frameworks/flex-config.xml";
    final VirtualFile configXmlFile = VfsUtil.findRelativeFile(configFileRelativePath, sdkRoot);
    if (configXmlFile != null) {
      try {
        return FlexUtils.findXMLElement(configXmlFile.getInputStream(), FlexSdkUtils.TARGET_PLAYER_ELEMENT);
      }
      catch (IOException e) {/*ignore*/}
    }
    return null;
  }

  public static void changeFlexSdkIfNeeded(final Module module, final String targetPlayerVersion) {
    if (!(module.getModuleType() instanceof FlexModuleType)) {
      return;
    }

    final ModuleEditor moduleEditor = FlexUtils.getModuleEditor(module, ModuleStructureConfigurable.getInstance(module.getProject()));
    final ModifiableRootModel modifiableRootModel = moduleEditor == null ? null : moduleEditor.getModifiableRootModel();
    final Sdk sdk = modifiableRootModel == null ? null : modifiableRootModel.getSdk();
    if (needToChangeSdk(sdk, targetPlayerVersion)) {
      final Sdk newSdk = findOrCreateProperSdk(module.getProject(), sdk, targetPlayerVersion);
      if (modifiableRootModel != null && newSdk != null) {
        modifiableRootModel.setSdk(newSdk);

        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            // rather hacky way to update selected SDK in combobox (ModuleJdkConfigurable.myCbModuleJdk)
            ProjectStructureConfigurable.getInstance(module.getProject()).getProjectJdksModel().getMulticaster()
              .sdkChanged(newSdk, newSdk.getName());
          }
        }, ModalityState.current(), new Condition() {
          public boolean value(Object o) {
            return module.isDisposed() ||
                   FlexUtils.getModuleEditor(module, ModuleStructureConfigurable.getInstance(module.getProject())) == null;
          }
        });
      }
    }
  }

  public static boolean needToChangeSdk(final Sdk currentSdk, final String targetPlayerVersion) {
    if (currentSdk == null || !(currentSdk.getSdkType() instanceof FlexSdkType)) {
      return false;
    }

    final VirtualFile requiredRoot = findRequiredPlayerglobalSwc(currentSdk, targetPlayerVersion);
    if (requiredRoot == null) {
      // current sdk may not match but nothing can be done with it
      return false;
    }

    return !ArrayUtil.contains(requiredRoot, currentSdk.getRootProvider().getFiles(OrderRootType.CLASSES));
  }

  @Nullable
  private static VirtualFile findRequiredPlayerglobalSwc(final Sdk sdk, final String targetPlayerVersion) {
    final VirtualFile sdkRoot = sdk.getHomeDirectory();
    if (sdkRoot == null) {
      return null;
    }

    final Pair<String, String> majorMinor = getPlayerMajorMinorVersion(targetPlayerVersion);

    VirtualFile playerglobalSwc = VfsUtil.findRelativeFile(
      "/frameworks/libs/player/" + majorMinor.first + "." + majorMinor.second + "/playerglobal.swc", sdkRoot);
    if (playerglobalSwc == null) {
      playerglobalSwc = VfsUtil.findRelativeFile("/frameworks/libs/player/" + majorMinor.first + "/playerglobal.swc", sdkRoot);
    }
    return playerglobalSwc == null ? null : JarFileSystem.getInstance().getJarRootForLocalFile(playerglobalSwc);
  }

  public static Pair<String, String> getPlayerMajorMinorVersion(final @NotNull String targetPlayerVersion) throws NumberFormatException {
    final int firstDotIndex = targetPlayerVersion.indexOf('.');

    if (firstDotIndex != -1) {
      final int secondDotIndex = targetPlayerVersion.indexOf('.', firstDotIndex + 1);
      final String majorVersion = targetPlayerVersion.substring(0, firstDotIndex);
      return secondDotIndex == -1
             ? Pair.create(majorVersion, targetPlayerVersion.substring(firstDotIndex + 1))
             : Pair.create(majorVersion, targetPlayerVersion.substring(firstDotIndex + 1, secondDotIndex));
    }
    else {
      return Pair.create(targetPlayerVersion, "0");
    }
  }

  @Nullable
  public static Sdk findOrCreateProperSdk(final Project project, final Sdk currentSdk, final String targetPlayerVersion) {
    if (needToChangeSdk(currentSdk, targetPlayerVersion)) {
      final VirtualFile requiredPlayerglobalSwc = findRequiredPlayerglobalSwc(currentSdk, targetPlayerVersion);
      if (requiredPlayerglobalSwc == null) {
        // current sdk may not match but nothing can be done with it
        return null;
      }

      final Sdk anotherSdk = findProperSdk(currentSdk, requiredPlayerglobalSwc);
      if (anotherSdk != null) {
        return anotherSdk;
      }
      else {
        return createProperSdk(project, currentSdk, requiredPlayerglobalSwc);
      }
    }
    else {
      return currentSdk;
    }
  }

  @Nullable
  private static Sdk findProperSdk(final Sdk currentSdk, final VirtualFile requiredPlayerglobalSwc) {
    final VirtualFile sdkRoot = currentSdk.getHomeDirectory();
    if (sdkRoot == null) {
      return null;
    }
    for (final Sdk sdk : FlexSdkUtils.getAllFlexSdks()) {
      if (sdkRoot.equals(sdk.getHomeDirectory()) &&
          ArrayUtil.contains(requiredPlayerglobalSwc, sdk.getRootProvider().getFiles(OrderRootType.CLASSES))) {
        return sdk;
      }
    }
    return null;
  }

  private static Sdk createProperSdk(final Project project, final Sdk currentSdk, final VirtualFile requiredPlayerglobalSwc) {
    final Sdk newSdk;
    try {
      newSdk = (Sdk)currentSdk.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }

    final SdkModificator modificator1 = newSdk.getSdkModificator();
    final VirtualFile[] roots = modificator1.getRoots(OrderRootType.CLASSES);
    for (final VirtualFile root : roots) {
      if (root.getPath().contains("/frameworks/libs/player/") && root.getPath().endsWith("/playerglobal.swc!/")) {
        modificator1.removeRoot(root, OrderRootType.CLASSES);
      }
    }

    modificator1.addRoot(requiredPlayerglobalSwc, OrderRootType.CLASSES);
    modificator1.commitChanges();

    // need to take modificator once again after playerglobal.swc commit in order to get correct result for getTargetPlayerVersion(newSdk)
    final SdkModificator modificator2 = newSdk.getSdkModificator();
    final String currentSdkName = currentSdk.getName();
    final String postfixRegexp = " [(]player [0-9]+(\\.[0-9]+)?[)]";
    final Pair<String, String> majorMinor = getPlayerMajorMinorVersion(getTargetPlayerVersion(newSdk));
    final String suggestedPostfix = " (player " + majorMinor.first + "." + majorMinor.second + ")";
    final String suggestedSdkName = currentSdkName.matches(".*" + postfixRegexp + ".*")
                                    ? currentSdkName.replaceAll(postfixRegexp, suggestedPostfix)
                                    : currentSdkName + suggestedPostfix;

    modificator2.setName(createUniqueSdkName(suggestedSdkName));
    modificator2.commitChanges();


    // need to add it explicitly, otherwise ProjectSdksModel may return false in isModified() and newSdk will be lost
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        ProjectJdkTable.getInstance().addJdk(newSdk);
      }
    });

    // the same as last 3 lines of ProjectSdksModel.doAdd()
    final ProjectSdksModel projectJdksModel = ProjectStructureConfigurable.getInstance(project).getProjectJdksModel();
    projectJdksModel.getProjectSdks().put(newSdk, newSdk);
    JdkListConfigurable.getInstance(project).addJdkNode(newSdk, true);
    projectJdksModel.getMulticaster().sdkAdded(newSdk);

    return newSdk;
  }

  private static String createUniqueSdkName(final String suggestedName) {
    final Sdk[] sdks = ProjectJdkTable.getInstance().getAllJdks();
    final Set<String> names = new HashSet<String>();
    for (Sdk jdk : sdks) {
      names.add(jdk.getName());
    }
    String newSdkName = suggestedName;
    int i = 0;
    while (names.contains(newSdkName)) {
      newSdkName = suggestedName + " (" + (++i) + ")";
    }
    return newSdkName;
  }

  public static void updateTargetPlayerIfMajorOrMinorVersionDiffers(final FlexBuildConfiguration config, final Sdk sdk) {
    if (sdk != null && sdk.getSdkType() instanceof IFlexSdkType) {
      final String version = getTargetPlayerVersion(sdk);
      if (!majorAndMinorVersionEqual(config.TARGET_PLAYER_VERSION, version)) {
        config.TARGET_PLAYER_VERSION = version;
      }
    }
  }

  public static boolean isEqual(String version1, String version2) {
    while (version1.endsWith(".")) version1 = version1.substring(0, version1.length() - 1);
    while (version2.endsWith(".")) version2 = version2.substring(0, version2.length() - 1);
    return version1.equals(version2);
  }
}
