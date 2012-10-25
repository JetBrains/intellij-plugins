package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.flex.model.bc.LinkageType;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.build.FlexCompiler;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.BuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: ksafonov
 */
public class FlexOrderEnumerationHandler extends OrderEnumerationHandler {

  public static Key<FlexBuildConfiguration> FORCE_BC = Key.create(FlexOrderEnumerationHandler.class.getName() + ".forceBc");

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

  private static class ModuleData {
    private Set<FlexBuildConfiguration> bcs = new HashSet<FlexBuildConfiguration>();
    private boolean accessibleInProduction = false; // true if this module accessible by non-test dependency types

    public void addBc(FlexBuildConfiguration bc, boolean production) {
      bcs.add(bc);
      accessibleInProduction |= production;
    }
  }

  @Nullable
  private final Map<Module, ModuleData> myActiveConfigurations;
  @Nullable private final Module myRootModule;


  public FlexOrderEnumerationHandler(@Nullable Module module) {
    myRootModule = module;
    if (module == null) {
      myActiveConfigurations = null;
      return;
    }

    myActiveConfigurations = new HashMap<Module, ModuleData>();
    // last argument can be whatever
    processModuleWithBuildConfiguration(module, null, myActiveConfigurations, new HashSet<FlexBuildConfiguration>(), true);
  }

  // configuration is null for root module (one for which scope is being computed)
  private static void processModuleWithBuildConfiguration(@NotNull Module module, @Nullable FlexBuildConfiguration configuration,
                                                          Map<Module, ModuleData> modules2activeConfigurations,
                                                          Set<FlexBuildConfiguration> processedConfigurations,
                                                          boolean productionDependency) {
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return;
    }


    final boolean isRootModule = configuration == null;
    if (isRootModule) {
      configuration = getActiveConfiguration(module);
    }

    if (configuration == null || !processedConfigurations.add(configuration)) {
      return;
    }

    ModuleData moduleData = modules2activeConfigurations.get(module);
    if (moduleData == null) {
      modules2activeConfigurations.put(module, moduleData = new ModuleData());
    }
    moduleData.addBc(configuration, productionDependency);
    for (DependencyEntry entry : configuration.getDependencies().getEntries()) {
      if (!(entry instanceof BuildConfigurationEntry)) {
        continue;
      }

      final LinkageType linkageType = entry.getDependencyType().getLinkageType();
      if (linkageType == LinkageType.LoadInRuntime) {
        continue;
      }

      FlexBuildConfiguration dependencyBc = ((BuildConfigurationEntry)entry).findBuildConfiguration();
      if (dependencyBc == null || !FlexCompiler.checkDependencyType(configuration, dependencyBc, linkageType)) {
        continue;
      }
      if (!isRootModule && !BCUtils.isTransitiveDependency(linkageType)) {
        continue;
      }

      Module dependencyModule = ((BuildConfigurationEntry)entry).findModule();
      if (dependencyModule == null || dependencyModule == module) {
        continue;
      }
      processModuleWithBuildConfiguration(dependencyModule, dependencyBc, modules2activeConfigurations, processedConfigurations,
                                          entry.getDependencyType().getLinkageType() != LinkageType.Test);
    }
  }

  private static FlexBuildConfiguration getActiveConfiguration(final Module module) {
    final FlexBuildConfiguration forced = FORCE_BC.get(module);
    return forced != null ? forced : FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
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
      if (module != myRootModule) {
        // never add transitive dependency to Flex SDK
        return AddDependencyType.DO_NOT_ADD;
      }

      if (myActiveConfigurations == null) {
        return AddDependencyType.DEFAULT;
      }

      ModuleData moduleData = myActiveConfigurations.get(module);
      for (FlexBuildConfiguration bc : moduleData.bcs) {
        if (bc.getSdk() != null) {
          return AddDependencyType.DEFAULT;
        }
      }
      return AddDependencyType.DO_NOT_ADD;
    }

    Collection<FlexBuildConfiguration> accessibleConfigurations;
    if (myActiveConfigurations != null) {
      ModuleData moduleData = myActiveConfigurations.get(module);
      accessibleConfigurations = moduleData != null ? moduleData.bcs : Collections.<FlexBuildConfiguration>emptyList();
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

      if (library.getKind() == FlexLibraryType.FLEX_LIBRARY) {
        return FlexProjectRootsUtil.dependOnLibrary(accessibleConfigurations, library, module != myRootModule, settings.isProductionOnly())
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
        ModuleData moduleData = myActiveConfigurations.get(dependencyModule);
        return moduleData != null && (moduleData.accessibleInProduction || !settings.isProductionOnly())
               ? AddDependencyType.DEFAULT
               : AddDependencyType.DO_NOT_ADD;
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
    final FlexBuildConfiguration bc = getActiveConfiguration(forModule);
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
