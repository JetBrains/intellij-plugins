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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Tapestry page.
 */
public class Page extends PresentationLibraryElement implements ExternalizableToTemplate {

    private IResource[] _templateCache;

    Page(Library library, IJavaClassType pageClass, TapestryProject project) throws NotTapestryElementException {
        super(library, pageClass, project);
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

        List<IResource> templates = new ArrayList<IResource>();
        String packageName = getElementClass().getFullyQualifiedName().substring(0, getElementClass().getFullyQualifiedName().lastIndexOf('.'));

        // Search in the classpath
        Collection<IResource> resources = getProject().getResourceFinder().findLocalizedClasspathResource(
                PathUtils.packageIntoPath(packageName, true) +
                        PathUtils.getLastPathElement(getName()) +
                        "." + TapestryConstants.TEMPLATE_FILE_EXTENSION, true
        );

        if (resources.size() > 0) {
            for (IResource template : resources)
                templates.add(template);
        }

        // Search in web application context
        resources = getProject().getResourceFinder().findLocalizedContextResource(getName() + "." + TapestryConstants.TEMPLATE_FILE_EXTENSION);
        if (resources.size() > 0) {
            for (IResource template : resources)
                templates.add(template);
        }

        _templateCache = templates.toArray(new IResource[templates.size()]);

        return _templateCache;
    }

    public String getTemplateRepresentation(String namespacePrefix) throws Exception {
        return ExternalizeToTemplateChain.getInstance().externalize(this, namespacePrefix);
    }
}
