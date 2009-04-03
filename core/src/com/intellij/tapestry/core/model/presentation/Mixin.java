package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.resource.IResource;

public class Mixin extends ParameterReceiverElement {

    Mixin(Library library, IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
        super(library, componentClass, project);
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowsTemplate() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public IResource[] getTemplate() {
        return new IResource[0];
    }

    /**
     * {@inheritDoc}
     */
    public IResource[] getMessageCatalog() {
        return new IResource[0];
    }
}
