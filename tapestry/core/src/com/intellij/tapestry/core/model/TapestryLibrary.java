package com.intellij.tapestry.core.model;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a Tapestry library.
 */
public final class TapestryLibrary implements Comparable {
  private final String _id;
  private final String _basePackage;
  private final TapestryProject _project;
  @Nullable
  private final String myShortName;

  public TapestryLibrary(String id, String basePackage, TapestryProject project) {
    this(id, basePackage, project, null);
  }

  public TapestryLibrary(String id, String basePackage, TapestryProject project, @Nullable String libraryShortName) {
    _id = id;
    _basePackage = basePackage;
    _project = project;
    myShortName = libraryShortName;
  }

  public String getId() {
    return _id;
  }

  public String getBasePackage() {
    return _basePackage;
  }

  @Nullable
  public String getShortName() {
    return myShortName;
  }

  /**
   * Finds all components of this library.
   *
   * @return all components of this library.
   */
  public Map<String, PresentationLibraryElement> getComponents() {
    return findElements(TapestryConstants.COMPONENTS_PACKAGE, _basePackage);
  }

  /**
   * Finds all abstract components of this library.
   *
   * @return all abstract components of this library.
   */
  public Map<String, PresentationLibraryElement> getAbstractComponents() {
    return findElements(TapestryConstants.BASE_PACKAGE, _basePackage);
  }

  /**
   * Finds all pages of this library.
   *
   * @return all pages of this library.
   */
  public Map<String, PresentationLibraryElement> getPages() {
    return findElements(TapestryConstants.PAGES_PACKAGE, _basePackage);
  }

  /**
   * Finds all mixins of this library.
   *
   * @return all mixins of this library.
   */
  public Map<String, PresentationLibraryElement> getMixins() {
    return findElements(TapestryConstants.MIXINS_PACKAGE, _basePackage);
  }

  ///**
  // * Finds the Tapestry IoC module builder of this library.
  // *
  // * @return the Tapestry IoC module builder of this library.
  // */
  /*public ModuleBuilder getModuleBuilder() {
      if (getBasePackage().equals(TapestryConstants.CORE_LIBRARY_PACKAGE)) {
          return new ModuleBuilder(_project.getJavaTypeFinder().findType("org.apache.tapestry5.services.TapestryModule", true), _project);
      }

      if (getBasePackage().equals(TapestryConstants.IOC_LIBRARY_PACKAGE)) {
          return new ModuleBuilder(_project.getJavaTypeFinder().findType("org.apache.tapestry5.ioc.services.TapestryIOCModule", true), _project);
      }

      return new ModuleBuilder(
              _project.getJavaTypeFinder().findType(
                      _project.getApplicationRootPackage() +
                              "." +
                              StringUtils.capitalize(_project.getTapestryFilterName()) +
                              TapestryConstants.MODULE_BUILDER_SUFIX, false
              ), _project
      );
  }*/

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(Object object) {
    return getBasePackage().compareTo(((TapestryLibrary)object).getBasePackage());
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object object) {
    return object instanceof TapestryLibrary && getBasePackage().equals(((TapestryLibrary)object).getBasePackage());
  }

  /**
   * {@inheritDoc}
   */
  public int hashCode() {
    return getBasePackage().hashCode();
  }

  /**
   * Finds all Tapestry elements implemented under the given base package.
   *
   * @param componentsOrPages configures this to look for pages or components.
   * @param basePackage       the base package.
   * @return all the Tapestry elements implemented under the given package.
   */
  private Map<String, PresentationLibraryElement> findElements(String componentsOrPages, String basePackage) {
    Map<String, PresentationLibraryElement> components = CollectionFactory.createCaseInsensitiveStringMap();

    for (IJavaClassType type : _project.getJavaTypeFinder().findTypesInPackageRecursively(basePackage + "." + componentsOrPages, true)) {
      try {
        PresentationLibraryElement element = PresentationLibraryElement.createElementInstance(this, type, _project);
        components.put(element.getName(), element);
      }
      catch (NotTapestryElementException e) {
        //ignore
      }
    }

    return components;
  }
}
