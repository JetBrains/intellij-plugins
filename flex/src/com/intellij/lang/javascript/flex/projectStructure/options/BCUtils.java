package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitPrecompileTask;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationForm;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.flex.sdk.RslUtil;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
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

  public static boolean isTransitiveDependency(final LinkageType linkageType) {
    return linkageType == LinkageType.Include;
  }

  public static String getWrapperFileName(final FlexIdeBuildConfiguration bc) {
    return FileUtil.getNameWithoutExtension(PathUtil.getFileName(bc.getActualOutputFilePath())) + ".html";
  }

  public static String getGeneratedAirDescriptorName(final FlexIdeBuildConfiguration bc, final AirPackagingOptions packagingOptions) {
    final String suffix = packagingOptions instanceof AirDesktopPackagingOptions
                          ? "-descriptor.xml"
                          : packagingOptions instanceof AndroidPackagingOptions ? "-android-descriptor.xml"
                                                                                : "-ios-descriptor.xml";
    return FileUtil.getNameWithoutExtension(PathUtil.getFileName(bc.getActualOutputFilePath())) + suffix;
  }

  @Nullable
  public static String getBCSpecifier(final FlexIdeBuildConfiguration bc) {
    if (!bc.isTempBCForCompilation()) return null;
    if (isFlexUnitBC(bc)) return "flexunit";
    if (isRuntimeStyleSheetBC(bc)) return PathUtil.getFileName(bc.getMainClass());
    return StringUtil.getShortName(bc.getMainClass());
  }

  public static boolean isFlexUnitBC(final FlexIdeBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().endsWith(FlexUnitPrecompileTask.FLEX_UNIT_LAUNCHER);
  }

  public static boolean canHaveRLMsAndRuntimeStylesheets(final FlexIdeBuildConfiguration bc) {
    return canHaveRLMsAndRuntimeStylesheets(bc.getOutputType(), bc.getTargetPlatform());
  }

  public static boolean canHaveRLMsAndRuntimeStylesheets(final OutputType outputType, final TargetPlatform targetPlatform) {
    return outputType == OutputType.Application && targetPlatform != TargetPlatform.Mobile;
  }

  public static boolean isRuntimeStyleSheetBC(final FlexIdeBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().toLowerCase().endsWith(".css");
  }

  public static boolean canHaveResourceFiles(final BuildConfigurationNature nature) {
    return nature.isApp();
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
   * If <code>LinkageType.Default</code> is returned then use {@link #getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return <code>null</code> if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(String swcPath, FlexIdeBuildConfiguration bc) {
    return getSdkEntryLinkageType(bc.getSdk(), swcPath, bc.getNature(), bc.getDependencies().getTargetPlayer(),
                                  bc.getDependencies().getComponentSet());
  }

  /**
   * If <code>LinkageType.Default</code> is returned then use {@link #getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return <code>null</code> if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(final Sdk sdk,
                                                   final String swcPath,
                                                   final BuildConfigurationNature bcNature,
                                                   final String targetPlayer,
                                                   final ComponentSet componentSet) {
    LOG.assertTrue(!swcPath.endsWith(JarFileSystem.JAR_SEPARATOR), "plain local filesystem path is expected");

    if (swcPath.endsWith("/frameworks/libs/air/airglobal.swc")) {
      return bcNature.isWebPlatform() ? null : LinkageType.External;
    }

    if (swcPath.endsWith("/playerglobal.swc") && swcPath.contains("/frameworks/libs/player/")) {
      if (swcPath.endsWith("/frameworks/libs/player/" + targetPlayer + "/playerglobal.swc")) {
        return bcNature.isWebPlatform() ? LinkageType.External : null;
      }
      return null;
    }

    final boolean swcIncluded;

    final int lastSlashIndex = swcPath.lastIndexOf('/');
    if (lastSlashIndex <= 0 || lastSlashIndex == swcPath.length() - 1) {
      LOG.error("Unexpected Flex SDK root: " + swcPath);
    }
    final String swcName = swcPath.substring(lastSlashIndex + 1);
    final String folderPath = swcPath.substring(0, lastSlashIndex);

    if (folderPath.endsWith("/frameworks/libs")) {
      swcIncluded = isSwcFromLibsFolderIncluded(bcNature, componentSet, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/air")) {
      swcIncluded = isSwcFromAirFolderIncluded(bcNature, componentSet, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/mobile")) {
      swcIncluded = isSwcFromMobileFolderIncluded(bcNature, swcName);
    }
    else if (folderPath.endsWith("/frameworks/libs/mx")) {
      swcIncluded = isSwcFromMxFolderIncluded(bcNature, componentSet, swcName);
    }
    else if (folderPath.contains("/frameworks/themes/")) {
      swcIncluded = false;
    }
    else {
      if (!ApplicationManager.getApplication().isUnitTestMode()) {
        LOG.warn("Unknown Flex SDK root: " + swcPath);
      }
      swcIncluded = true;
    }

    if (!swcIncluded) return null;

    // our difference from FB is that in case of library _ALL_ SWCs from SDK are external by default (except *global.swc)
    if (bcNature.isLib()) return LinkageType.Default;

    return RslUtil.canBeRsl(sdk, swcPath) ? LinkageType.Default : LinkageType.Merged;
  }

  private static boolean isSwcFromLibsFolderIncluded(final BuildConfigurationNature bcNature,
                                                     final ComponentSet componentSet,
                                                     final String swcName) {
    if (swcName.equals("advancedgrids.swc")) {
      return !(bcNature.isMobilePlatform() || bcNature.pureAS || componentSet == ComponentSet.SparkOnly);
    }

    if (swcName.equals("authoringsupport.swc")) {
      return true;
    }

    if (swcName.equals("charts.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.equals("core.swc")) {
      return bcNature.pureAS;
    }

    if (swcName.equals("datavisualization.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.endsWith("flash-integration.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.equals("flex.swc")) {
      return bcNature.pureAS;
    }

    if (swcName.endsWith("framework.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.endsWith("osmf.swc")) {
      return true;
    }

    if (swcName.endsWith("rpc.swc")) {
      return !bcNature.pureAS;
    }

    if (swcName.endsWith("spark.swc")) {
      return !bcNature.pureAS && (bcNature.isMobilePlatform() || componentSet != ComponentSet.MxOnly);
    }

    if (swcName.endsWith("spark_dmv.swc")) {
      return !bcNature.pureAS && !bcNature.isMobilePlatform() && componentSet == ComponentSet.SparkAndMx;
    }

    if (swcName.endsWith("sparkskins.swc")) {
      return !bcNature.pureAS && !bcNature.isMobilePlatform() && componentSet != ComponentSet.MxOnly;
    }

    if (swcName.endsWith("textLayout.swc")) {
      return true;
    }

    if (swcName.endsWith("utilities.swc")) {
      return true;
    }

    if (swcName.equals("automation.swc") ||
        swcName.equals("automation_agent.swc") ||
        swcName.equals("automation_dmv.swc") ||
        swcName.equals("automation_flashflexkit.swc") ||
        swcName.equals("qtp.swc")) {
      // additionally installed on top of Flex SDK 3.x
      return true;
    }

    LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs' folder: " + swcName);
    return true;
  }

  private static boolean isSwcFromAirFolderIncluded(final BuildConfigurationNature bcNature,
                                                    final ComponentSet componentSet,
                                                    final String swcName) {
    if (bcNature.isMobilePlatform()) {
      return swcName.equals("servicemonitor.swc");
    }

    if (bcNature.isDesktopPlatform()) {
      if (swcName.equals("airframework.swc")) {
        return !bcNature.pureAS;
      }

      if (swcName.equals("airspark.swc")) {
        return !bcNature.pureAS && componentSet != ComponentSet.MxOnly;
      }

      return true;
    }

    return false;
  }

  private static boolean isSwcFromMobileFolderIncluded(final BuildConfigurationNature bcNature, final String swcName) {
    if (!swcName.equals("mobilecomponents.swc")) {
      LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs/mobile' folder: " + swcName);
    }
    return !bcNature.pureAS && bcNature.isMobilePlatform();
  }

  private static boolean isSwcFromMxFolderIncluded(final BuildConfigurationNature bcNature,
                                                   final ComponentSet componentSet,
                                                   final String swcName) {
    if (!swcName.equals("mx.swc")) {
      LOG.warn("Unknown SWC in '<Flex SDK>/frameworks/libs/mx' folder: " + swcName);
    }
    return !bcNature.isMobilePlatform() && !bcNature.pureAS && componentSet != ComponentSet.SparkOnly;
  }

  public static boolean isApplicable(final BuildConfigurationNature dependantNature,
                                     final BuildConfigurationNature dependencyNature,
                                     final LinkageType linkageType) {
    switch (dependencyNature.outputType) {
      case Application:
        return false;
      case Library:
        return ArrayUtil.contains(linkageType, LinkageType.getSwcLinkageValues());
      case RuntimeLoadedModule:
        return linkageType == LinkageType.LoadInRuntime && !dependantNature.isLib();
      default:
        assert false;
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
        final String[] availablePlayersArray = ArrayUtil.toStringArray(availablePlayers);
        targetPlayerCombo.setModel(new DefaultComboBoxModel(availablePlayersArray));
        //noinspection SuspiciousMethodCalls
        if (selectedItem != null && availablePlayers.contains(selectedItem)) {
          targetPlayerCombo.setSelectedItem(selectedItem);
        }
        else {
          targetPlayerCombo.setSelectedItem(TargetPlayerUtils.getMaximumVersion(availablePlayersArray));
        }
      }
    }
    else {
      targetPlayerCombo.setModel(new DefaultComboBoxModel(ArrayUtil.EMPTY_STRING_ARRAY));
    }
  }

  public static JSClassChooserDialog.PublicInheritor getMainClassFilter(@NotNull Module module,
                                                                        @Nullable FlexIdeBuildConfiguration bc,
                                                                        final boolean rlm,
                                                                        final boolean includeTests,
                                                                        boolean caching) {
    final String baseClass = rlm ? FlashRunConfigurationForm.MODULE_BASE_CLASS_NAME
                                 : FlashRunConfigurationForm.SPRITE_CLASS_NAME;
    final GlobalSearchScope filterScope = bc == null
                                          ? module.getModuleWithDependenciesAndLibrariesScope(includeTests)
                                          : FlexUtils.getModuleWithDependenciesAndLibrariesScope(module, bc, includeTests);
    return new JSClassChooserDialog.PublicInheritor(module.getProject(), baseClass, filterScope, true, caching);
  }

  public static boolean isValidMainClass(final Module module,
                                         @Nullable final FlexIdeBuildConfiguration buildConfiguration,
                                         final JSClass clazz,
                                         final boolean includeTests) {
    return getMainClassFilter(module, buildConfiguration, false, includeTests, false).value(clazz);
  }

  public static SimpleColoredText renderBuildConfiguration(@NotNull FlexIdeBuildConfiguration bc,
                                                           @Nullable String moduleName) {
    return renderBuildConfiguration(bc, moduleName, false);
  }

  public static SimpleColoredText renderBuildConfiguration(@NotNull FlexIdeBuildConfiguration bc,
                                                           @Nullable String moduleName,
                                                           boolean bold) {
    SimpleColoredText text = new SimpleColoredText();
    text.append(bc.getShortText(), bold ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES);
    text.append(" (" + bc.getDescription() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
    if (moduleName != null) {
      text.append(" - " + moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    return text;
  }

  public static SimpleColoredText renderMissingBuildConfiguration(@NotNull String bcName, @Nullable String moduleName) {
    return moduleName != null
           ? new SimpleColoredText(bcName + " - " + moduleName, SimpleTextAttributes.ERROR_ATTRIBUTES)
           : new SimpleColoredText(bcName, SimpleTextAttributes.ERROR_ATTRIBUTES);
  }
}
