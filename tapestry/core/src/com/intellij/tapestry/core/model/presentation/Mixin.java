package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.IResource;

public class Mixin extends ParameterReceiverElement {

    Mixin(TapestryLibrary library, IJavaClassType componentClass, TapestryProject project) throws NotTapestryElementException {
        super(library, componentClass, project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowsTemplate() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResource[] getTemplate() {
        return IResource.EMPTY_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResource[] getMessageCatalog() {
        return IResource.EMPTY_ARRAY;
    }
}
