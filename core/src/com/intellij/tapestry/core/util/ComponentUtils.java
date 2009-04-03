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

import java.io.File;

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
     * @throws NotFoundException when the class can't be found.
     */
    public static IJavaClassType findClassFromTemplate(IResource template, TapestryProject project) throws NotFoundException {
        String resourcePath = template.getFile().getAbsolutePath();
        String templateFilename = LocalizationUtils.unlocalizeFileName(template.getName());
        Library applicationLibrary = project.getApplicationLibrary();

        resourcePath = PathUtils.removeLastFilePathElement(resourcePath, false) + File.separator + templateFilename;

        for (PresentationLibraryElement component : applicationLibrary.getComponents().values())
            if (component.getTemplate().length > 0 && LocalizationUtils.unlocalizeFileName(component.getTemplate()[0].getFile().getAbsolutePath()).equals(resourcePath)) {
                return component.getElementClass();
            }

        for (PresentationLibraryElement page : applicationLibrary.getPages().values())
            if (page.getTemplate().length > 0 && LocalizationUtils.unlocalizeFileName(page.getTemplate()[0].getFile().getAbsolutePath()).equals(resourcePath)) {
                return page.getElementClass();
            }

        throw new NotFoundException();
    }

    /**
     * Checks if a tag in a HTML document is a component tag.
     *
     * @param tag the tag to check.
     * @return <code>true</code> if the given tag is a opening or closing tag of a Tapestry component, <code>false</code> otherwise.
     */
    public static boolean isComponentTag(XmlTag tag) {
        return tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE) || hasTapestryNamespaceAttribute(tag.getAttributes());
    }

    private static boolean hasTapestryNamespaceAttribute(XmlAttribute[] attributes) {
        for (XmlAttribute attribute : attributes)
            if (attribute.getLocalName().length() > 0 && attribute.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) {
                return true;
            }

        return false;
    }
}
