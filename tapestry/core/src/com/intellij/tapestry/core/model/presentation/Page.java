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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Tapestry page.
 */
public class Page extends PresentationLibraryElement implements ExternalizableToTemplate {

    private IResource[] _templateCache;

    Page(TapestryLibrary library, IJavaClassType pageClass, TapestryProject project) throws NotTapestryElementException {
        super(library, pageClass, project);
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

        String packageName = getElementClass().getFullyQualifiedName().substring(0, getElementClass().getFullyQualifiedName().lastIndexOf('.'));

        // Search in the classpath
        Collection<IResource> resources = getProject().getResourceFinder().findLocalizedClasspathResource(
                PathUtils.packageIntoPath(packageName, true) +
                        PathUtils.getLastPathElement(getName()) +
                        "." + TapestryConstants.TEMPLATE_FILE_EXTENSION, true
        );

        List<IResource> templates = new ArrayList<>(resources);

        // Search in web application context
        resources = getProject().getResourceFinder().findLocalizedContextResource(getName() + "." + TapestryConstants.TEMPLATE_FILE_EXTENSION);
        templates.addAll(resources);

        _templateCache = templates.toArray(IResource.EMPTY_ARRAY);

        return _templateCache;
    }

    @Override
    public String getTemplateRepresentation(String namespacePrefix) throws Exception {
        return ExternalizeToTemplateChain.getInstance().externalize(this, namespacePrefix);
    }
}
