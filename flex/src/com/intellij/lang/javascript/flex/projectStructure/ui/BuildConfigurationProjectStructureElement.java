package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.build.FlashProjectStructureProblem;
import com.intellij.lang.javascript.flex.build.FlexCompiler;
import com.intellij.lang.javascript.flex.build.FlexCompilerHandler;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.*;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.navigation.Place;
import com.intellij.util.Consumer;
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

  public BuildConfigurationProjectStructureElement(final ModifiableFlexIdeBuildConfiguration bc,
                                                   final Module module,
                                                   final @NotNull StructureConfigurableContext context) {
    super(context);
    myBc = bc;
    myModule = module;
  }

  @Override
  public String getPresentableName() {
    return FlexCommonBundle.message("bc.0.module.1", myBc.getName(), myModule.getName());
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
    final FlexIdeBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    final FlexProjectConfigurationEditor editor = configurator.getConfigEditor();
    assert editor != null;

    /*
    final SdkEntry sdkEntry = myBc.getDependencies().getSdkEntry();
    if (sdkEntry == null) {
      Pair<String, Object> location =
        Pair.<String, Object>create(FlexIdeBCConfigurable.LOCATION_ON_TAB, DependenciesConfigurable.Location.SDK);

      PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);
      problemsHolder.registerProblem(FlexBundle.message("bc.problem.no.sdk"), null, ProjectStructureProblemType.error("sdk"),
                                     place, null);
    }
    else {
      if (FlexSdkUtils.findFlexOrFlexmojosSdk(sdkEntry.getName()) == null) {
        Pair<String, Object> location =
          Pair.<String, Object>create(FlexIdeBCConfigurable.LOCATION_ON_TAB, DependenciesConfigurable.Location.SDK);

        PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);
        problemsHolder.registerProblem(FlexBundle.message("bc.problem.sdk.not.found", sdkEntry.getName()), null,
                                       ProjectStructureProblemType.error("flex-bc-sdk"), place, null);
      }
    }
    */

    checkDependencies(problemsHolder, editor);

    checkSameOutputPaths(problemsHolder, configurator, editor);

    checkIfBCOutputUsedAs3rdPartyLib(problemsHolder, configurator, editor);

    FlexCompiler.checkConfiguration(myModule, myBc, true, new Consumer<FlashProjectStructureProblem>() {
      public void consume(final FlashProjectStructureProblem problem) {
        PlaceInProjectStructure place =
          new PlaceInBuildConfiguration(BuildConfigurationProjectStructureElement.this, problem.tabName, problem.locationOnTab);
        problemsHolder.registerProblem(problem.errorMessage, null, ProjectStructureProblemType.error(problem.errorId), place, null);
      }
    });
  }

  private void checkDependencies(final ProjectStructureProblemsHolder problemsHolder, final FlexProjectConfigurationEditor editor) {
    final ModulesConfigurator modulesConfigurator = myContext.getModulesConfigurator();

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
          Object location = DependenciesConfigurable.Location.TableEntry.forBc(moduleName, bcName);
          PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);
          problemsHolder.registerProblem(errorMessage, null, ProjectStructureProblemType.error("flex-bc-dependency-bc"), place, null);
        }
      }
    }
  }

  private void checkSameOutputPaths(final ProjectStructureProblemsHolder problemsHolder,
                                    final FlexIdeBCConfigurator configurator,
                                    final FlexProjectConfigurationEditor editor) {
    final String outputPath = myBc.getActualOutputFilePath();
    final List<ModifiableFlexIdeBuildConfiguration> bcs = configurator.getBCsByOutputPath(outputPath);
    if (bcs != null && bcs.size() > 1) {
      final StringBuilder buf = new StringBuilder();
      for (ModifiableFlexIdeBuildConfiguration bc : bcs) {
        if (bc != myBc) {
          if (buf.length() > 0) buf.append(", ");
          buf.append(FlexBundle.message("0.module.1", bc.getName(), editor.getModule(bc).getName()));
        }
      }

      final String message =
        FlexBundle.message("same.output.files.as.in.bcs", bcs.size() - 1, buf.toString(), FileUtil.toSystemDependentName(outputPath));

      final FlexIdeBCConfigurable.Location location = FlexIdeBCConfigurable.Location.OutputFileName;
      final PlaceInProjectStructure placeInPS = new PlaceInBuildConfiguration(this, myBc.getName(), location);
      problemsHolder.registerProblem(message, null, ProjectStructureProblemType.warning(location.errorId), placeInPS, null);
    }
  }

  private void checkIfBCOutputUsedAs3rdPartyLib(final ProjectStructureProblemsHolder problemsHolder,
                                                final FlexIdeBCConfigurator configurator,
                                                final FlexProjectConfigurationEditor editor) {
    for (final DependencyEntry entry : myBc.getDependencies().getEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry =
          FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, editor.getModifiableRootModel(myModule));
        if (orderEntry != null) {
          checkIfBCOutputUsedAs3rdPartyLib(problemsHolder, configurator, entry, orderEntry.getRootFiles(OrderRootType.CLASSES));
        }
      }
      else if (entry instanceof SharedLibraryEntry) {
        final Library library = FlexProjectRootsUtil.findOrderEntry(myModule.getProject(), (SharedLibraryEntry)entry);
        if (library != null) {
          checkIfBCOutputUsedAs3rdPartyLib(problemsHolder, configurator, entry, library.getFiles((OrderRootType.CLASSES)));
        }
      }
    }
  }

  private void checkIfBCOutputUsedAs3rdPartyLib(final ProjectStructureProblemsHolder problemsHolder,
                                                final FlexIdeBCConfigurator configurator,
                                                final DependencyEntry entry,
                                                final VirtualFile[] classesRoots) {
    for (VirtualFile libFile : classesRoots) {
      final VirtualFile realFile = FlexCompilerHandler.getRealFile(libFile);
      if (realFile != null && !realFile.isDirectory() && "swc".equalsIgnoreCase(realFile.getExtension())) {
        final List<ModifiableFlexIdeBuildConfiguration> bcs = configurator.getBCsByOutputPath(realFile.getPath());
        if (bcs != null && !bcs.isEmpty()) {
          final ModifiableFlexIdeBuildConfiguration otherLibBC = bcs.get(0);
          final FlexProjectConfigurationEditor editor = configurator.getConfigEditor();
          assert editor != null;
          final Module otherLibModule = editor.getModule(otherLibBC);
          final String message =
            FlexBundle.message("own.lib.used.as.3rd.party", realFile.getName(), otherLibBC.getName(), otherLibModule.getName());

          final Object location =
            entry instanceof ModuleLibraryEntry
            ? DependenciesConfigurable.Location.TableEntry.forModuleLibrary(((ModuleLibraryEntry)entry).getLibraryId())
            : DependenciesConfigurable.Location.TableEntry
              .forSharedLibrary(((SharedLibraryEntry)entry).getLibraryLevel(), ((SharedLibraryEntry)entry).getLibraryName());
          final PlaceInProjectStructure placeInPS = new PlaceInBuildConfiguration(this, DependenciesConfigurable.TAB_NAME, location);

          final String quickFixName = FlexBundle.message("instead.setup.dependency.on.bc", otherLibBC.getName(), otherLibModule.getName());
          final ConfigurationErrorQuickFix quickFix = new ConfigurationErrorQuickFix(quickFixName) {
            public void performFix() {
              final FlexIdeBCConfigurable configurable = configurator.getBCConfigurable(myBc);
              final DependenciesConfigurable dependenciesConfigurable = configurable.getDependenciesConfigurable();
              final FlexIdeBCConfigurable otherLibConfigurable = configurator.getBCConfigurable(otherLibBC);

              final LinkageType linkageType = entry.getDependencyType().getLinkageType();
              dependenciesConfigurable.addBCDependency(otherLibConfigurable, linkageType);

              if (entry instanceof ModuleLibraryEntry) {
                dependenciesConfigurable.removeDependency(((ModuleLibraryEntry)entry).getLibraryId());
              }
              else {
                dependenciesConfigurable
                  .removeDependency(((SharedLibraryEntry)entry).getLibraryLevel(), ((SharedLibraryEntry)entry).getLibraryName());
              }

              final Place place = configurator.getPlaceFor(myModule, myBc.getName());
              place.putPath(CompositeConfigurable.TAB_NAME, DependenciesConfigurable.TAB_NAME);
              place.putPath(FlexIdeBCConfigurable.LOCATION_ON_TAB,
                            DependenciesConfigurable.Location.TableEntry.forBc(otherLibConfigurable));
              ProjectStructureConfigurable.getInstance(myModule.getProject()).navigateTo(place, true);
            }
          };

          final String errorId =
            entry instanceof ModuleLibraryEntry ? ((ModuleLibraryEntry)entry).getLibraryId() : ((SharedLibraryEntry)entry).getLibraryName();
          problemsHolder.registerProblem(message, null, ProjectStructureProblemType.warning(errorId), placeInPS, quickFix);
        }
      }
    }
  }

  @Override
  public List<ProjectStructureElementUsage> getUsagesInElement() {
    FlexIdeBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    final FlexProjectConfigurationEditor editor = configurator.getConfigEditor();
    assert editor != null;
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
