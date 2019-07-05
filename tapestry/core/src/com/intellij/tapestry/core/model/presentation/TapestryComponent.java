package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.model.externalizable.ExternalizableToTemplate;
import com.intellij.tapestry.core.model.externalizable.totemplatechain.ExternalizeToTemplateChain;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.util.PathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A Tapestry component.
 */
public class TapestryComponent extends ParameterReceiverElement implements ExternalizableToTemplate {

  private IResource[] _templateCache;

  protected TapestryComponent(@NotNull TapestryLibrary library, @NotNull IJavaClassType componentClass, @NotNull TapestryProject project)
    throws NotTapestryElementException {
    super(library, componentClass, project);
  }

  protected TapestryComponent(@NotNull IJavaClassType componentClass, @NotNull TapestryProject project) throws NotTapestryElementException {
    super(null, componentClass, project);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean allowsTemplate() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IResource[] getTemplate() {
    if (_templateCache != null && checkAllValidResources(_templateCache)) {
      return _templateCache;
    }

    final String fqn = getElementClass().getFullyQualifiedName();
    String packageName = fqn.substring(0, fqn.lastIndexOf('.'));

    // Search in the classpath
    Collection<IResource> resources = getProject().getResourceFinder().findLocalizedClasspathResource(
      PathUtils.packageIntoPath(packageName, true) +
      PathUtils.getLastPathElement(getName()) +
      "." +
      TapestryConstants.TEMPLATE_FILE_EXTENSION, true);

    _templateCache = resources.toArray(IResource.EMPTY_ARRAY);

    return _templateCache;
  }

  @Override
  public String getTemplateRepresentation(String namespacePrefix) throws Exception {
    return ExternalizeToTemplateChain.getInstance().externalize(this, namespacePrefix);
  }
}
