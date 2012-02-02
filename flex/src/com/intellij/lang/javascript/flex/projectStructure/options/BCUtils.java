package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ksafonov
 */
public class BCUtils {

  private static LinkageType[] FLEX_LIB_LINKAGES = {LinkageType.Default, LinkageType.Merged, LinkageType.External};
  private static LinkageType[] FLEX_MOBILE_APP_LINKAGES = {LinkageType.Default};
  private static LinkageType[] FLEX_WEB_OR_DESKTOP_APP_LINKAGES = {LinkageType.Default, LinkageType.Merged, LinkageType.RSL};
  private static LinkageType[] AS_LINKAGES = {LinkageType.Default};

  private static Logger LOG = Logger.getInstance(BCUtils.class);

  public static String getGeneratedAirDescriptorName(final FlexIdeBuildConfiguration config, final AirPackagingOptions packagingOptions) {
    final String suffix = packagingOptions instanceof AirDesktopPackagingOptions
                          ? "-descriptor.xml"
                          : packagingOptions instanceof AndroidPackagingOptions ? "-android-descriptor.xml"
                                                                                : "-ios-descriptor.xml";
    return FileUtil.getNameWithoutExtension(config.getOutputFileName()) + suffix;
  }

  public static LinkageType[] getSuitableFrameworkLinkages(BuildConfigurationNature nature) {
    return nature.pureAS
           ? AS_LINKAGES
           : nature.isLib()
             ? FLEX_LIB_LINKAGES
             : nature.isMobilePlatform()
               ? FLEX_MOBILE_APP_LINKAGES
               : FLEX_WEB_OR_DESKTOP_APP_LINKAGES;
  }

  public static LinkageType getDefaultFrameworkLinkage(final String sdkVersion,
                                                       final BuildConfigurationNature nature) {
    return nature.isLib()
           ? LinkageType.External
           : nature.pureAS
             ? LinkageType.Merged
             : nature.isWebPlatform()
               ? StringUtil.compareVersionNumbers(sdkVersion, "4") >= 0 // Web Flex App
                 ? LinkageType.RSL      // Flex 4
                 : LinkageType.Merged   // Flex 3
               : LinkageType.Merged;  // AIR Flex App (Desktop or Mobile)
  }

  /**
   * If <code>LinkageType.Default</code> is returned then use {@link #getDefaultFrameworkLinkage(BuildConfigurationNature)} to get real value.
   *
   * @return <code>null</code> if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(String path, FlexIdeBuildConfiguration bc) {
    return getSdkEntryLinkageType(path, bc.getNature(), bc.getDependencies().getTargetPlayer(), bc.getDependencies().getComponentSet());
  }

  /**
   * If <code>LinkageType.Default</code> is returned then use {@link #getDefaultFrameworkLinkage(BuildConfigurationNature)} to get real value.
   *
   * @return <code>null</code> if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(final String path,
                                                   final BuildConfigurationNature bcNature,
                                                   final String targetPlayer,
                                                   final ComponentSet componentSet) {
    LOG.assertTrue(!path.endsWith(JarFileSystem.JAR_SEPARATOR), "plain local filesystem path is expected");

    if (path.endsWith("/frameworks/libs/air/airglobal.swc")) {
      return bcNature.isWebPlatform() ? null : LinkageType.External;
    }

    if (path.endsWith("/playerglobal.swc") && path.contains("/frameworks/libs/player/")) {
      if (path.endsWith("/frameworks/libs/player/" + targetPlayer + "/playerglobal.swc")) {
        return bcNature.isWebPlatform() ? LinkageType.External : null;
      }
      return null;
    }

    final LinkageType linkageType;

    final int lastSlashIndex = path.lastIndexOf('/');
    if (lastSlashIndex <= 0 || lastSlashIndex == path.length() - 1) {
      LOG.error("Unexpected Flex SDK root: " + path);
    }
    final String swcName = path.substring(lastSlashIndex + 1);
    final String folderPath = path.substring(0, lastSlashIndex);

    if (folderPath.endsWith("/frameworks/libs")) {
      linkageType = getLibraryFromLibsFolderLinkage(bcNature, componentSet, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/air")) {
      linkageType = getAirLibraryLinkage(bcNature, componentSet, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/mobile")) {
      linkageType = getMobileLibraryLinkage(bcNature, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/mx")) {
      linkageType = getMxLibraryLinkage(bcNature, componentSet, swcName);
    }
    else if (folderPath.contains("/frameworks/themes/")) {
      linkageType = null;
    }
    else {
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        LOG.warn("Unknown Flex SDK root: " + path);
      }
      linkageType = LinkageType.Merged;
    }

    // our difference from FB is that in case of library _ALL_ SWCs from SDK are external by default (except *global.swc)
    return bcNature.isLib() && linkageType != null ? LinkageType.Default : linkageType;
  }

  @Nullable
  private static LinkageType getLibraryFromLibsFolderLinkage(final BuildConfigurationNature bcNature,
                                                             final ComponentSet componentSet,
                                                             final String swcName) {
    if (swcName.equals("advancedgrids.swc")) {
      return bcNature.isMobilePlatform() || bcNature.pureAS || componentSet == ComponentSet.SparkOnly
             ? null
             : LinkageType.Default;
    }

    if (swcName.equals("authoringsupport.swc")) {
      return LinkageType.Merged;
    }

    if (swcName.equals("charts.swc")) {
      return bcNature.pureAS ? null : LinkageType.Default;
    }

    if (swcName.equals("core.swc")) {
      return bcNature.pureAS ? LinkageType.Merged : null;
    }

    if (swcName.equals("datavisualization.swc")) {
      return bcNature.pureAS ? null : LinkageType.Merged;
    }

    if (swcName.endsWith("flash-integration.swc")) {
      return bcNature.pureAS ? null : LinkageType.Merged;
    }

    if (swcName.equals("flex.swc")) {
      return bcNature.pureAS ? LinkageType.Merged : null;
    }

    if (swcName.endsWith("framework.swc")) {
      return bcNature.pureAS ? null : LinkageType.Default;
    }

    if (swcName.endsWith("osmf.swc")) {
      return LinkageType.Default;
    }

    if (swcName.endsWith("rpc.swc")) {
      return bcNature.pureAS ? null : LinkageType.Default;
    }

    if (swcName.endsWith("spark.swc")) {
      return bcNature.pureAS ? null
                             : (bcNature.isMobilePlatform() || componentSet != ComponentSet.MxOnly)
                               ? LinkageType.Default
                               : null;
    }

    if (swcName.endsWith("spark_dmv.swc")) {
      return !bcNature.pureAS && !bcNature.isMobilePlatform() && componentSet == ComponentSet.SparkAndMx
             ? LinkageType.Default : null;
    }

    if (swcName.endsWith("sparkskins.swc")) {
      return !bcNature.pureAS && !bcNature.isMobilePlatform() && componentSet != ComponentSet.MxOnly
             ? LinkageType.Default : null;
    }

    if (swcName.endsWith("textLayout.swc")) {
      return LinkageType.Default;
    }

    if (swcName.endsWith("utilities.swc")) {
      return LinkageType.Merged;
    }

    if (swcName.equals("automation.swc") ||
        swcName.equals("automation_agent.swc") ||
        swcName.equals("automation_dmv.swc") ||
        swcName.equals("automation_flashflexkit.swc") ||
        swcName.equals("qtp.swc")) {
      // additionally installed on top of Flex SDK 3.x
      return LinkageType.Merged;
    }

    LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs' folder: " + swcName);
    return LinkageType.Merged;
  }

  @Nullable
  private static LinkageType getAirLibraryLinkage(final BuildConfigurationNature bcNature,
                                                  final ComponentSet componentSet,
                                                  final String swcName) {
    if (bcNature.isMobilePlatform()) {
      return swcName.equals("servicemonitor.swc") ? LinkageType.Merged : null;
    }

    if (bcNature.isDesktopPlatform()) {
      if (swcName.equals("airframework.swc")) {
        return bcNature.pureAS ? null : LinkageType.Merged;
      }

      if (swcName.equals("airspark.swc")) {
        return bcNature.pureAS || componentSet == ComponentSet.MxOnly ? null : LinkageType.Merged;
      }

      return LinkageType.Merged;
    }

    return null;
  }

  @Nullable
  private static LinkageType getMobileLibraryLinkage(final BuildConfigurationNature bcNature, final String swcName) {
    if (!swcName.equals("mobilecomponents.swc")) {
      LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs/mobile' folder: " + swcName);
    }
    return bcNature.pureAS || !bcNature.isMobilePlatform() ? null : LinkageType.Merged;
  }

  @Nullable
  private static LinkageType getMxLibraryLinkage(final BuildConfigurationNature bcNature,
                                                 final ComponentSet componentSet,
                                                 final String swcName) {
    if (!swcName.equals("mx.swc")) {
      LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs/mx' folder: " + swcName);
    }
    return bcNature.isMobilePlatform() || bcNature.pureAS || componentSet == ComponentSet.SparkOnly
           ? null
           : LinkageType.Default;
  }

  public static boolean isApplicable(BuildConfigurationNature dependantNature, LinkageType dependencyLinkageType) {
    if (dependencyLinkageType == LinkageType.Default) {
      return false;
    }

    if (dependantNature.isLib()) {
      return dependencyLinkageType != LinkageType.LoadInRuntime;
    }
    else {
      return true;
    }
  }

  public static boolean isApplicableForDependency(BuildConfigurationNature dependantNature, OutputType dependencyOutputType) {
    if (dependantNature.isLib()) {
      return dependencyOutputType == OutputType.Library;
    }
    else {
      return dependencyOutputType == OutputType.Library ||
             dependencyOutputType == OutputType.RuntimeLoadedModule;
    }
  }

  public static void updateAvailableTargetPlayers(Sdk currentSdk, final JComboBox targetPlayerCombo) {
    if (currentSdk != null && currentSdk.getSdkType() instanceof FlexmojosSdkType) return;
    final String sdkHome = currentSdk == null ? null : currentSdk.getHomePath();
    final String playerFolderPath = sdkHome == null ? null : sdkHome + "/frameworks/libs/player";
    if (playerFolderPath != null) {
      final VirtualFile playerDir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          final VirtualFile playerFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(playerFolderPath);
          if (playerFolder != null && playerFolder.isDirectory()) {
            playerFolder.refresh(false, true);
            return playerFolder;
          }
          return null;
        }
      });

      if (playerDir != null) {
        final Collection<String> availablePlayers = new ArrayList<String>(2);
        FlexSdkUtils.processPlayerglobalSwcFiles(playerDir, new Processor<VirtualFile>() {
          public boolean process(final VirtualFile playerglobalSwcFile) {
            availablePlayers.add(playerglobalSwcFile.getParent().getName());
            return true;
          }
        });

        final Object selectedItem = targetPlayerCombo.getSelectedItem();
        targetPlayerCombo.setModel(new DefaultComboBoxModel(ArrayUtil.toStringArray(availablePlayers)));
        if (selectedItem != null) {
          targetPlayerCombo.setSelectedItem(selectedItem);
        }
      }
    }
  }
}
