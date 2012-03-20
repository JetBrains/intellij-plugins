package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.*;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ksafonov
 */
public class BuildConfigurationProjectStructureElement extends ProjectStructureElement {

  private final ModifiableFlexIdeBuildConfiguration myBc;
  private final Module myModule;

  public BuildConfigurationProjectStructureElement(final ModifiableFlexIdeBuildConfiguration bc, Module module,
                                                   @NotNull StructureConfigurableContext context) {
    super(context);
    myBc = bc;
    myModule = module;
  }

  @Override
  public String getPresentableName() {
    return FlexBundle.message("bc.structure.element.presentable.name", myBc.getName(), myModule.getName());
  }

  @Override
  public String getTypeName() {
    return FlexBundle.message("bc.structure.element.type.name");
  }

  @Override
  public String getId() {
    return "flex_bc:" + myBc.getName() + "\t" + myModule.getName();
  }

  @Override
  public void check(final ProjectStructureProblemsHolder problemsHolder) {
    FlexProjectConfigurationEditor editor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    final ModulesConfigurator modulesConfigurator = myContext.getModulesConfigurator();

    final SdkEntry sdkEntry = myBc.getDependencies().getSdkEntry();
    if (sdkEntry == null) {
      Pair<String, Object> location =
        Pair.<String, Object>create(DependenciesConfigurable.LOCATION, DependenciesConfigurable.Location.SDK);

      PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);
      problemsHolder.registerProblem(FlexBundle.message("bc.problem.no.sdk"), null, ProjectStructureProblemType.error("flex-bc-sdk"),
                                     place, null);
    }
    else {
      if (FlexSdkUtils.findFlexOrFlexmojosSdk(sdkEntry.getName()) == null) {
        Pair<String, Object> location =
          Pair.<String, Object>create(DependenciesConfigurable.LOCATION, DependenciesConfigurable.Location.SDK);

        PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);
        problemsHolder.registerProblem(FlexBundle.message("bc.problem.sdk.not.found", sdkEntry.getName()), null,
                                       ProjectStructureProblemType.error("flex-bc-sdk"), place, null);
      }
    }

    for (DependencyEntry entry : myBc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final String moduleName = ((BuildConfigurationEntry)entry).getModuleName();
        final String bcName = ((BuildConfigurationEntry)entry).getBcName();
        final Module module = modulesConfigurator.getModule(moduleName);
        String errorMessage = null;
        if (module == null) {
          errorMessage = FlexBundle.message("bc.problem.dependency.module.not.found", moduleName);
        }
        else if (ContainerUtil.find(editor.getConfigurations(module), new Condition<ModifiableFlexIdeBuildConfiguration>() {
          @Override
          public boolean value(final ModifiableFlexIdeBuildConfiguration configuration) {
            return bcName.equals(configuration.getName());
          }
        }) == null) {
          errorMessage = FlexBundle.message("bc.problem.dependency.bc.not.found", bcName, moduleName);
        }

        if (errorMessage != null) {
          Pair<String, Object> location =

            Pair.<String, Object>create(DependenciesConfigurable.LOCATION,
                                        DependenciesConfigurable.Location.TableEntry.forBc(moduleName, bcName));
          PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);
          problemsHolder.registerProblem(errorMessage, null, ProjectStructureProblemType.error("flex-bc-dependency-bc"), place, null);
        }
      }
    }
  }

  @Override
  public List<ProjectStructureElementUsage> getUsagesInElement() {
    FlexIdeBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    final FlexProjectConfigurationEditor editor = configurator.getConfigEditor();
    final ModulesConfigurator modulesConfigurator = myContext.getModulesConfigurator();

    final List<ProjectStructureElementUsage> usages = new ArrayList<ProjectStructureElementUsage>();
    for (DependencyEntry dependencyEntry : myBc.getDependencies().getEntries()) {
      if (dependencyEntry instanceof SharedLibraryEntry) {
        String libraryName = ((SharedLibraryEntry)dependencyEntry).getLibraryName();
        String libraryLevel = ((SharedLibraryEntry)dependencyEntry).getLibraryLevel();
        final Library library = myContext.getLibrary(libraryName, libraryLevel);
        if (library != null) {
          usages.add(new UsageInBcDependencies(this, new LibraryProjectStructureElement(myContext, library)) {
            @Override
            public void removeSourceElement() {
              libraryReplaced(library, null);
            }

            @Override
            public void replaceElement(final ProjectStructureElement newElement) {
              libraryReplaced(library, ((LibraryProjectStructureElement)newElement).getLibrary());
            }
          });
        }
      }
      else if (dependencyEntry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)dependencyEntry;
        Module module = modulesConfigurator.getModule(bcEntry.getModuleName());
        if (module != null) {
          final ModifiableFlexIdeBuildConfiguration bc =
            ContainerUtil.find(editor.getConfigurations(module), new Condition<ModifiableFlexIdeBuildConfiguration>() {
              @Override
              public boolean value(final ModifiableFlexIdeBuildConfiguration configuration) {
                return bcEntry.getBcName().equals(configuration.getName());
              }
            });
          if (bc != null) {
            usages.add(new UsageInBcDependencies(this, new BuildConfigurationProjectStructureElement(bc, module, myContext)) {
              @Override
              public void removeSourceElement() {
                // ignore as editor already listens to BC removal
              }

              @Override
              public void replaceElement(final ProjectStructureElement newElement) {
                throw new UnsupportedOperationException();
              }
            });
          }
        }
        bcEntry.findBuildConfiguration();
      }
    }

    Sdk sdk = myBc.getSdk();
    if (sdk != null) {
      usages.add(new UsageInBcDependencies(this, new SdkProjectStructureElement(myContext, sdk)) {
        @Override
        public void removeSourceElement() {
          myBc.getDependencies().setSdkEntry(null);
        }

        @Override
        public void replaceElement(final ProjectStructureElement newElement) {
          throw new UnsupportedOperationException();
        }
      });
    }
    return usages;
  }

  protected void libraryReplaced(@NotNull final Library library, @Nullable final Library replacement) {
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof BuildConfigurationProjectStructureElement &&
           myModule.equals(((BuildConfigurationProjectStructureElement)obj).myModule) &&
           myBc.equals(((BuildConfigurationProjectStructureElement)obj).myBc);
  }

  @Override
  public int hashCode() {
    return myModule.hashCode() ^ myBc.hashCode();
  }

  public StructureConfigurableContext getContext() {
    return myContext;
  }

  public Module getModule() {
    return myModule;
  }

  public FlexIdeBuildConfiguration getBc() {
    return myBc;
  }

  @Override
  public String getDescription() {
    return myBc.getDescription();
  }
}
