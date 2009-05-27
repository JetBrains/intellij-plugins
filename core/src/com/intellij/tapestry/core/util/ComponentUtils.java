package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.xml.XmlAttribute;
import com.intellij.tapestry.core.resource.xml.XmlTag;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * Utility methods related to Tapestry components.
 */
public class ComponentUtils {

  /**
   * Finds the component class from it's template.
   *
   * @param template the component template.
   * @param project  the project to look in.
   * @return the component class.
   */
  @Nullable
  public static IJavaClassType findClassFromTemplate(@NotNull IResource template, @NotNull TapestryProject project) {
    String resourcePath = template.getFile().getAbsolutePath();
    String templateFilename = LocalizationUtils.unlocalizeFileName(template.getName());

    Library applicationLibrary;
    try {
      applicationLibrary = project.getApplicationLibrary();
    }
    catch (NotFoundException e) {
      return null;
    }

    resourcePath = PathUtils.removeLastFilePathElement(resourcePath, false) + File.separator + templateFilename;

    IJavaClassType type = checkFirstResourceForEach(resourcePath, applicationLibrary.getComponents().values());
    if(type != null) return type;
    return checkFirstResourceForEach(resourcePath, applicationLibrary.getPages().values());
  }

  @Nullable
  public static IJavaClassType checkFirstResourceForEach(String resourcePath, Collection<PresentationLibraryElement> components) {
    for (PresentationLibraryElement component : components) {
      final IResource[] resources = component.getTemplate();
      if (resources.length > 0 &&
          LocalizationUtils.unlocalizeFileName(resources[0].getFile().getAbsolutePath()).equals(resourcePath)) {
        return component.getElementClass();
      }
    }
    return null;
  }

  /**
   * Checks if a tag in a HTML document is a component tag.
   *
   * @param tag the tag to check.
   * @return <code>true</code> if the given tag is a opening or closing tag of a Tapestry component, <code>false</code> otherwise.
   */
  public static boolean isComponentTag(XmlTag tag) {
    return tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)
           || hasTapestryNamespaceAttribute(tag.getAttributes());
  }

  private static boolean hasTapestryNamespaceAttribute(XmlAttribute[] attributes) {
    for (XmlAttribute attribute : attributes) {
      if (attribute.getLocalName().length() > 0
          && attribute.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) return true;
    }
    return false;
  }
}
