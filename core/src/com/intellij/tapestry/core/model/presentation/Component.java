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
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Tapestry component.
 */
public class Component extends ParameterReceiverElement implements ExternalizableToTemplate {

    private IResource[] _templateCache;

    protected Component(@NonNull Library library, @NonNull IJavaClassType componentClass, @NonNull TapestryProject project) throws NotTapestryElementException {
        super(library, componentClass, project);
    }

    protected Component(@NonNull IJavaClassType componentClass, @NonNull TapestryProject project) throws NotTapestryElementException {
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

        String packageName = getElementClass().getFullyQualifiedName().substring(0, getElementClass().getFullyQualifiedName().lastIndexOf('.'));

        // Search in the classpath
        Collection<IResource> resources = getProject().getResourceFinder().findLocalizedClasspathResource(
                PathUtils.packageIntoPath(packageName, true) +
                        PathUtils.getLastPathElement(getName()) +
                        TapestryConstants.TEMPLATE_FILE_EXTENSION, true
        );

        if (resources.size() > 0) {
            List<IResource> templates = new ArrayList<IResource>();

            for (IResource template : resources)
                templates.add(template);

            _templateCache = templates.toArray(new IResource[templates.size()]);

            return _templateCache;
        } else {
            _templateCache = new IResource[0];
        }

        return _templateCache;
    }

    public String getTemplateRepresentation(String namespacePrefix) throws Exception {
        return ExternalizeToTemplateChain.getInstance().externalize(this, namespacePrefix);
    }
}
