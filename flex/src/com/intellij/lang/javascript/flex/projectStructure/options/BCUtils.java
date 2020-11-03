// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.*;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.FlexProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationForm;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.PublicInheritorFilter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ksafonov
 */
public final class BCUtils {

  private static final LinkageType[] FLEX_LIB_LINKAGES = {LinkageType.Default, LinkageType.Merged, LinkageType.External};
  private static final LinkageType[] FLEX_MOBILE_APP_LINKAGES = {LinkageType.Default};
  private static final LinkageType[] FLEX_WEB_OR_DESKTOP_APP_LINKAGES = {LinkageType.Default, LinkageType.Merged, LinkageType.RSL};
  private static final LinkageType[] AS_LINKAGES = {LinkageType.Default};

  private static final Logger LOG = Logger.getInstance(BCUtils.class);

  public static boolean isTransitiveDependency(final LinkageType linkageType) {
    return linkageType == LinkageType.Include;
  }

  public static String getWrapperFileName(final FlexBuildConfiguration bc) {
    return FileUtilRt.getNameWithoutExtension(PathUtil.getFileName(bc.getActualOutputFilePath())) + ".html";
  }

  public static String getGeneratedAirDescriptorName(final FlexBuildConfiguration bc, final AirPackagingOptions packagingOptions) {
    final String suffix = packagingOptions instanceof AirDesktopPackagingOptions
                          ? "-descriptor.xml"
                          : packagingOptions instanceof AndroidPackagingOptions ? "-android-descriptor.xml"
                                                                                : "-ios-descriptor.xml";
    return FileUtilRt.getNameWithoutExtension(PathUtil.getFileName(bc.getActualOutputFilePath())) + suffix;
  }

  @Nullable
  public static String getBCSpecifier(final FlexBuildConfiguration bc) {
    if (!bc.isTempBCForCompilation()) return null;
    if (isFlexUnitBC(bc)) return "flexunit";
    if (isRLMTemporaryBC(bc)) return "module " + StringUtil.getShortName(bc.getMainClass());
    if (isRuntimeStyleSheetBC(bc)) return PathUtil.getFileName(bc.getMainClass());
    return StringUtil.getShortName(bc.getMainClass());
  }

  public static boolean isFlexUnitBC(final FlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().endsWith(FlexCommonUtils.FLEX_UNIT_LAUNCHER);
  }

  public static boolean canHaveRLMsAndRuntimeStylesheets(final FlexBuildConfiguration bc) {
    return FlexCommonUtils.canHaveRLMsAndRuntimeStylesheets(bc.getOutputType(), bc.getTargetPlatform());
  }

  public static boolean isRLMTemporaryBC(final FlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getOutputType() == OutputType.RuntimeLoadedModule;
  }

  public static boolean isRuntimeStyleSheetBC(final FlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && StringUtil.toLowerCase(bc.getMainClass()).endsWith(".css");
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

  /**
   * If {@code LinkageType.Default} is returned then use {@link FlexCommonUtils#getDefaultFrameworkLinkage(String, BuildConfigurationNature)} to get real value.
   *
   * @return {@code null} if entry should not be included at all
   */
  @Nullable
  public static LinkageType getSdkEntryLinkageType(String swcPath, FlexBuildConfiguration bc) {
    final Sdk sdk = bc.getSdk();
    LOG.assertTrue(sdk != null);
    return FlexCommonUtils.getSdkEntryLinkageType(sdk.getHomePath(), swcPath, bc.getNature(), bc.getDependencies().getTargetPlayer(),
                                                  bc.getDependencies().getComponentSet());
  }

  public static boolean isApplicableForDependency(BuildConfigurationNature dependantNature, OutputType dependencyOutputType) {
    if (dependantNature.isLib()) {
      return dependencyOutputType == OutputType.Library;
    }
    else {
      return true;
    }
  }

  public static void updateAvailableTargetPlayers(Sdk currentSdk, final JComboBox targetPlayerCombo) {
    if (currentSdk != null && currentSdk.getSdkType() instanceof FlexmojosSdkType) return;
    final String sdkHome = currentSdk == null ? null : currentSdk.getHomePath();
    final String playerFolderPath = sdkHome == null ? null : sdkHome + "/frameworks/libs/player";
    if (playerFolderPath != null) {
      final VirtualFile playerDir = ApplicationManager.getApplication().runWriteAction((NullableComputable<VirtualFile>)() -> {
        final VirtualFile playerFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(playerFolderPath);
        if (playerFolder != null && playerFolder.isDirectory()) {
          playerFolder.refresh(false, true);
          return playerFolder;
        }
        return null;
      });

      if (playerDir != null) {
        final Collection<String> availablePlayers = new ArrayList<>(2);
        FlexSdkUtils.processPlayerglobalSwcFiles(playerDir, playerglobalSwcFile -> {
          availablePlayers.add(playerglobalSwcFile.getParent().getName());
          return true;
        });

        final Object selectedItem = targetPlayerCombo.getSelectedItem();
        final String[] availablePlayersArray = ArrayUtilRt.toStringArray(availablePlayers);
        targetPlayerCombo.setModel(new DefaultComboBoxModel(availablePlayersArray));
        //noinspection SuspiciousMethodCalls
        if (selectedItem != null && availablePlayers.contains(selectedItem)) {
          targetPlayerCombo.setSelectedItem(selectedItem);
        }
        else {
          targetPlayerCombo.setSelectedItem(FlexCommonUtils.getMaximumVersion(availablePlayersArray));
        }
      }
    }
    else {
      targetPlayerCombo.setModel(new DefaultComboBoxModel(ArrayUtilRt.EMPTY_STRING_ARRAY));
    }
  }

  public static PublicInheritorFilter getMainClassFilter(@NotNull Module module,
                                                                        @Nullable FlexBuildConfiguration bc,
                                                                        final boolean rlm,
                                                                        final boolean includeTests,
                                                                        boolean caching) {
    final String baseClass = rlm ? FlashRunConfigurationForm.MODULE_BASE_CLASS_NAME
                                 : FlashRunConfigurationForm.SPRITE_CLASS_NAME;
    final GlobalSearchScope filterScope = bc == null
                                          ? module.getModuleWithDependenciesAndLibrariesScope(includeTests)
                                          : FlexUtils.getModuleWithDependenciesAndLibrariesScope(module, bc, includeTests);
    return new PublicInheritorFilter(module.getProject(), baseClass, filterScope, true, caching);
  }

  public static boolean isValidMainClass(final Module module,
                                         @Nullable final FlexBuildConfiguration buildConfiguration,
                                         final JSClass clazz,
                                         final boolean includeTests) {
    return getMainClassFilter(module, buildConfiguration, false, includeTests, false).value(clazz);
  }

  public static SimpleColoredText renderBuildConfiguration(@NotNull FlexBuildConfiguration bc,
                                                           @Nullable String moduleName) {
    return renderBuildConfiguration(bc, moduleName, false);
  }

  public static SimpleColoredText renderBuildConfiguration(@NotNull FlexBuildConfiguration bc,
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

  public static String suggestRLMOutputPath(final String mainClass) {
    return mainClass.replace('.', '/') + ".swf";
  }

  public static void initTargetPlatformCombo(final JComboBox<TargetPlatform> targetPlatformCombo) {
    targetPlatformCombo.setModel(new DefaultComboBoxModel<>(TargetPlatform.values()));
    targetPlatformCombo.setRenderer(SimpleListCellRenderer.create((label, value, index) -> {
      label.setText(value.getPresentableText());
      label.setIcon(value.getIcon());
    }));
  }

  public static void initOutputTypeCombo(final JComboBox<OutputType> outputTypeCombo) {
    outputTypeCombo.setModel(new DefaultComboBoxModel<>(OutputType.values()));
    outputTypeCombo.setRenderer(SimpleListCellRenderer.create("", OutputType::getPresentableText));
  }

  public static List<String> getThemes(final Module module, final FlexBuildConfiguration bc) {
    final Sdk sdk = bc.getSdk();
    if (sdk == null) return Collections.emptyList();

    final CompilerOptionInfo info = CompilerOptionInfo.getOptionInfo("compiler.theme");

    String value = bc.getCompilerOptions().getOption(info.ID);
    if (value == null) value = FlexBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions().getOption(info.ID);
    if (value == null) {
      value = FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions().getOption(info.ID);
    }
    if (value == null) value = info.getDefaultValue(sdk.getVersionString(), bc.getNature(), bc.getDependencies().getComponentSet());

    return value == null
           ? Collections.emptyList()
           : StringUtil.split(FlexUtils.replacePathMacros(value, module, sdk.getHomePath()), CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }
}
