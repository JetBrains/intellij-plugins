// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.build.FlashProjectStructureProblem;
import com.intellij.lang.javascript.flex.build.FlexCompilationUtils;
import com.intellij.lang.javascript.flex.build.ValidateFlashConfigurationsPrecompileTask;
import com.intellij.lang.javascript.flex.projectStructure.FlexBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.navigation.Place;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuildConfigurationProjectStructureElement extends ProjectStructureElement {

  private final ModifiableFlexBuildConfiguration myBc;
  private final Module myModule;

  public BuildConfigurationProjectStructureElement(final ModifiableFlexBuildConfiguration bc,
                                                   final Module module,
                                                   final @NotNull StructureConfigurableContext context) {
    super(context);
    myBc = bc;
    myModule = module;
  }

  @Override
  public String getPresentableName() {
    return myBc.getName();
  }

  @NotNull
  @Override
  public String getPresentableText() {
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
    final FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    final FlexProjectConfigurationEditor editor = configurator.getConfigEditor();
    assert editor != null;

    checkDependencies(problemsHolder, editor);

    checkSameOutputPaths(problemsHolder, configurator, editor);

    checkIfBCOutputUsedAs3rdPartyLib(problemsHolder, configurator, editor);

    ValidateFlashConfigurationsPrecompileTask.checkConfiguration(myModule, myBc, true, problem -> {
      // actually this if-condition is always true because real model doesn't report FlexUnitOutputFolderProblem
      if (!(problem instanceof FlashProjectStructureProblem.FlexUnitOutputFolderProblem)) {
        PlaceInProjectStructure place =
          new PlaceInBuildConfiguration(this, problem.tabName, problem.locationOnTab);
        final ProjectStructureProblemType problemType = problem.severity == ProjectStructureProblemType.Severity.ERROR
          ? ProjectStructureProblemType.error(problem.errorId)
          : ProjectStructureProblemType.warning(problem.errorId);
        problemsHolder.registerProblem(problem.errorMessage, null, problemType, place, null);
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
        else if (ContainerUtil.find(editor.getConfigurations(module), configuration -> bcName.equals(configuration.getName())) == null) {
          errorMessage = FlexBundle.message("bc.problem.dependency.bc.not.found", bcName, moduleName);
        }

        if (errorMessage != null) {
          Object location = DependenciesConfigurable.Location.TableEntry.forBc(moduleName, bcName);
          PlaceInProjectStructure place = new PlaceInBuildConfiguration(this, DependenciesConfigurable.getTabName(), location);
          problemsHolder.registerProblem(errorMessage, null, ProjectStructureProblemType.error("flex-bc-dependency-bc"), place, null);
        }
      }
    }
  }

  private void checkSameOutputPaths(final ProjectStructureProblemsHolder problemsHolder,
                                    final FlexBCConfigurator configurator,
                                    final FlexProjectConfigurationEditor editor) {
    final String outputPath = myBc.getActualOutputFilePath();
    final List<ModifiableFlexBuildConfiguration> bcs = configurator.getBCsByOutputPath(outputPath);
    if (bcs != null && bcs.size() > 1) {
      final StringBuilder buf = new StringBuilder();
      for (ModifiableFlexBuildConfiguration bc : bcs) {
        if (bc != myBc) {
          if (buf.length() > 0) buf.append(", ");
          buf.append(FlexBundle.message("0.module.1", bc.getName(), editor.getModule(bc).getName()));
        }
      }

      final String message =
        FlexBundle.message("same.output.files.as.in.bcs", bcs.size() - 1, buf.toString(), FileUtil.toSystemDependentName(outputPath));

      final FlexBCConfigurable.Location location = FlexBCConfigurable.Location.OutputFileName;
      final PlaceInProjectStructure placeInPS = new PlaceInBuildConfiguration(this, myBc.getName(), location);
      problemsHolder.registerProblem(message, null, ProjectStructureProblemType.warning(location.errorId), placeInPS, null);
    }
  }

  private void checkIfBCOutputUsedAs3rdPartyLib(final ProjectStructureProblemsHolder problemsHolder,
                                                final FlexBCConfigurator configurator,
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
                                                final FlexBCConfigurator configurator,
                                                final DependencyEntry entry,
                                                final VirtualFile[] classesRoots) {
    for (VirtualFile libFile : classesRoots) {
      final VirtualFile realFile = FlexCompilationUtils.getRealFile(libFile);
      if (realFile != null && !realFile.isDirectory() && "swc".equalsIgnoreCase(realFile.getExtension())) {
        final List<ModifiableFlexBuildConfiguration> bcs = configurator.getBCsByOutputPath(realFile.getPath());
        if (bcs != null && !bcs.isEmpty()) {
          final ModifiableFlexBuildConfiguration otherLibBC = bcs.get(0);
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
          final PlaceInProjectStructure placeInPS = new PlaceInBuildConfiguration(this, DependenciesConfigurable.getTabName(), location);

          final String quickFixName = FlexBundle.message("instead.setup.dependency.on.bc", otherLibBC.getName(), otherLibModule.getName());
          final ConfigurationErrorQuickFix quickFix = new ConfigurationErrorQuickFix(quickFixName) {
            @Override
            public void performFix() {
              final FlexBCConfigurable configurable = configurator.getBCConfigurable(myBc);
              final DependenciesConfigurable dependenciesConfigurable = configurable.getDependenciesConfigurable();
              final FlexBCConfigurable otherLibConfigurable = configurator.getBCConfigurable(otherLibBC);

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
              place.putPath(CompositeConfigurable.TAB_NAME, DependenciesConfigurable.getTabName());
              place.putPath(FlexBCConfigurable.LOCATION_ON_TAB,
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
    FlexBCConfigurator configurator = FlexBuildConfigurationsExtension.getInstance().getConfigurator();
    final FlexProjectConfigurationEditor editor = configurator.getConfigEditor();
    assert editor != null;
    final ModulesConfigurator modulesConfigurator = myContext.getModulesConfigurator();

    final List<ProjectStructureElementUsage> usages = new ArrayList<>();
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
          final ModifiableFlexBuildConfiguration bc =
            ContainerUtil.find(editor.getConfigurations(module), configuration -> bcEntry.getBcName().equals(configuration.getName()));
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

  public FlexBuildConfiguration getBC() {
    return myBc;
  }

  @Override
  public String getDescription() {
    return myBc.getDescription();
  }
}
