package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.roots.impl.libraries.LibraryTableImplUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.*;

import javax.swing.*;

public abstract class UsageInBcDependencies extends ProjectStructureElementUsage {
  private final BuildConfigurationProjectStructureElement myContainingElement;
  private final ProjectStructureElement mySourceElement;

  public UsageInBcDependencies(final BuildConfigurationProjectStructureElement containingElement,
                               final ProjectStructureElement sourceElement) {
    myContainingElement = containingElement;
    mySourceElement = sourceElement;
  }

  @Override
  public ProjectStructureElement getSourceElement() {
    return mySourceElement;
  }

  @Override
  public ProjectStructureElement getContainingElement() {
    return myContainingElement;
  }

  @Override
  public String getPresentableName() {
    return myContainingElement.getBC().getName();
  }

  @Override
  public PlaceInProjectStructure getPlace() {
    if (mySourceElement instanceof LibraryProjectStructureElement) {
      Library library = ((LibraryProjectStructureElement)mySourceElement).getLibrary();
      final DependenciesConfigurable.Location.TableEntry tableEntry;
      if (LibraryTableImplUtil.MODULE_LEVEL.equals(library.getTable().getTableLevel())) {
        tableEntry = DependenciesConfigurable.Location.TableEntry.forModuleLibrary(FlexProjectRootsUtil.getLibraryId(library));
      }
      else {
        tableEntry = DependenciesConfigurable.Location.TableEntry.forSharedLibrary(library);
      }
      return new PlaceInBuildConfiguration(myContainingElement, DependenciesConfigurable.getTabName(), tableEntry);
    }
    else if (mySourceElement instanceof BuildConfigurationProjectStructureElement bcElement) {
      String moduleName = bcElement.getModule().getName();
      String bcName = bcElement.getBC().getName();
      DependenciesConfigurable.Location.TableEntry tableEntry = DependenciesConfigurable.Location.TableEntry.forBc(moduleName, bcName);
      return new PlaceInBuildConfiguration(myContainingElement, DependenciesConfigurable.getTabName(), tableEntry);
    }
    else if (mySourceElement instanceof SdkProjectStructureElement) {
      return new PlaceInBuildConfiguration(myContainingElement, DependenciesConfigurable.getTabName(), DependenciesConfigurable.Location.SDK);
    }

    assert false : mySourceElement;
    return null;
  }

  @Override
  public int hashCode() {
    return myContainingElement.hashCode() & mySourceElement.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof UsageInBcDependencies &&
           ((UsageInBcDependencies)obj).getContainingElement().equals(getContainingElement()) &&
           ((UsageInBcDependencies)obj).getSourceElement().equals(getSourceElement());
  }

  @Override
  public Icon getIcon() {
    return myContainingElement.getBC().getIcon();
  }
}
