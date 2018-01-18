package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.externalizable.ExternalizableToTemplate;
import com.intellij.tapestry.core.model.externalizable.totemplatechain.ExternalizeToTemplateChain;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.util.PathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Tapestry component.
 */
public class Component extends ParameterReceiverElement implements ExternalizableToTemplate {

  private IResource[] _templateCache;

  protected Component(@NotNull Library library, @NotNull IJavaClassType componentClass, @NotNull TapestryProject project)
    throws NotTapestryElementException {
    super(library, componentClass, project);
  }

  protected Component(@NotNull IJavaClassType componentClass, @NotNull TapestryProject project) throws NotTapestryElementException {
    super(null, componentClass, project);
  }

  /**
   * {@inheritDoc}
   */
  public boolean allowsTemplate() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
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

    if (resources.size() > 0) {
      List<IResource> templates = new ArrayList<>();
      for (IResource template : resources) {
        templates.add(template);
      }
      _templateCache = templates.toArray(IResource.EMPTY_ARRAY);
    }
    else {
      _templateCache = IResource.EMPTY_ARRAY;
    }

    return _templateCache;
  }

  public String getTemplateRepresentation(String namespacePrefix) throws Exception {
    return ExternalizeToTemplateChain.getInstance().externalize(this, namespacePrefix);
  }
}
