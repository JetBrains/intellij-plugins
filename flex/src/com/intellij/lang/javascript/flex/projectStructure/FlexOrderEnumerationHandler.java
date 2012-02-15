package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.build.FlexCompiler;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: ksafonov
 */
public class FlexOrderEnumerationHandler extends OrderEnumerationHandler {

  public static class FactoryImpl extends Factory {
    @Override
    public boolean isApplicable(@NotNull Project project) {
      return true;
    }

    @Override
    public boolean isApplicable(@NotNull Module module) {
      return ModuleType.get(module) == FlexModuleType.getInstance();
    }

    @Override
    public OrderEnumerationHandler createHandler(@Nullable Module module) {
      return new FlexOrderEnumerationHandler(module);
    }
  }

  // TODO our special handling for myWithoutJdk, myWithoutLibraries

  @Nullable
  private final MultiMap<Module, FlexIdeBuildConfiguration> myActiveConfigurations;
  @Nullable private final Module myRootModule;


  public FlexOrderEnumerationHandler(@Nullable Module module) {
    myRootModule = module;
    if (module == null) {
      myActiveConfigurations = null;
      return;
    }

    myActiveConfigurations = new MultiMap<Module, FlexIdeBuildConfiguration>() {
      @Override
      protected Collection<FlexIdeBuildConfiguration> createCollection() {
        return new HashSet<FlexIdeBuildConfiguration>();
      }
    };
    processModuleWithBuildConfiguration(module, null, myActiveConfigurations, new HashSet<FlexIdeBuildConfiguration>());
  }

  private static void processModuleWithBuildConfiguration(Module module, @Nullable FlexIdeBuildConfiguration configuration,
                                                          MultiMap<Module, FlexIdeBuildConfiguration> modules2activeConfigurations,
                                                          Set<FlexIdeBuildConfiguration> processedConfigurations) {
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return;
    }

    if (configuration == null) {
      configuration = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    }

    if (configuration == null || !processedConfigurations.add(configuration)) {
      return;
    }

    modules2activeConfigurations.putValue(module, configuration);
    for (DependencyEntry entry : configuration.getDependencies().getEntries()) {
      if (!(entry instanceof BuildConfigurationEntry)) {
        continue;
      }

      final LinkageType linkageType = entry.getDependencyType().getLinkageType();
      if (linkageType == LinkageType.LoadInRuntime) {
        continue;
      }

      FlexIdeBuildConfiguration dependencyBc = ((BuildConfigurationEntry)entry).findBuildConfiguration();
      if (dependencyBc == null || !FlexCompiler.checkDependencyType(configuration, dependencyBc, linkageType)) {
        continue;
      }

      Module dependencyModule = ((BuildConfigurationEntry)entry).findModule();
      if (dependencyModule == module) {
        continue;
      }
      processModuleWithBuildConfiguration(dependencyModule, dependencyBc, modules2activeConfigurations, processedConfigurations);
    }
  }

  @NotNull
  @Override
  public AddDependencyType shouldAddDependency(@NotNull OrderEntry orderEntry,
                                               @NotNull OrderEnumeratorSettings settings) {
    Module module = orderEntry.getOwnerModule();
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return super.shouldAddDependency(orderEntry, settings);
    }

    if (orderEntry instanceof ModuleSourceOrderEntry) {
      return AddDependencyType.DEFAULT;
    }

    if (orderEntry instanceof JdkOrderEntry) {
      // never add transitive dependency to Flex SDK
      return module == myRootModule ? AddDependencyType.DEFAULT : AddDependencyType.DO_NOT_ADD;
    }

    Collection<FlexIdeBuildConfiguration> accessibleConfigurations;
    if (myActiveConfigurations != null) {
      accessibleConfigurations = myActiveConfigurations.get(module);
    }
    else {
      // let all configurations be accessible in ProjectOrderEnumerator
      accessibleConfigurations = Arrays.asList(FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations());
    }
    if (orderEntry instanceof LibraryOrderEntry) {
      final LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
      if (library == null) {
        return AddDependencyType.DEFAULT;
      }

      if (library.getType() instanceof FlexLibraryType) {
        return FlexProjectRootsUtil.dependOnLibrary(accessibleConfigurations, library, module != myRootModule)
               ? AddDependencyType.DEFAULT
               : AddDependencyType.DO_NOT_ADD;
      }
      else {
        // foreign library
        return AddDependencyType.DO_NOT_ADD;
      }
    }
    else if (orderEntry instanceof ModuleOrderEntry) {
      final Module dependencyModule = ((ModuleOrderEntry)orderEntry).getModule();
      if (dependencyModule == null) {
        return AddDependencyType.DO_NOT_ADD;
      }
      if (myActiveConfigurations != null) {
        return myActiveConfigurations.containsKey(dependencyModule) ? AddDependencyType.DEFAULT : AddDependencyType.DO_NOT_ADD;
      }
      else {
        // let all modules dependencies be accessible in ProjectOrderEnumerator
        return AddDependencyType.DEFAULT;
      }
    }
    else {
      return AddDependencyType.DEFAULT;
    }
  }

  @Override
  public boolean addCustomRootsForLibrary(@NotNull final OrderEntry forOrderEntry,
                                          @NotNull final OrderRootType type,
                                          @NotNull final Collection<String> urls) {
    if (!(forOrderEntry instanceof JdkOrderEntry)) {
      return false;
    }

    if (myActiveConfigurations == null) {
      return false;
    }

    final Module forModule = forOrderEntry.getOwnerModule();
    final FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(forModule).getActiveConfiguration();
    final Sdk sdk = bc.getSdk();
    if (sdk == null || sdk.getSdkType() != FlexSdkType2.getInstance()) {
      return false;
    }

    final String[] allUrls = sdk.getRootProvider().getUrls(type);
    if (type != OrderRootType.CLASSES) {
      urls.addAll(Arrays.asList(allUrls));
      return true;
    }

    final List<String> themePaths = CompilerOptionInfo.getThemes(forModule, bc);
    final List<String> allAccessibleUrls = ContainerUtil.filter(allUrls, new Condition<String>() {
      @Override
      public boolean value(String s) {
        s = VirtualFileManager.extractPath(StringUtil.trimEnd(s, JarFileSystem.JAR_SEPARATOR));
        return BCUtils.getSdkEntryLinkageType(s, bc) != null || themePaths.contains(s);
      }
    });
    urls.addAll(new HashSet<String>(allAccessibleUrls));
    return true;
  }
}
