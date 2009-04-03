package com.intellij.tapestry.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.tapestry.core.exceptions.NotTapestryModuleException;
import com.intellij.tapestry.intellij.util.TapestryUtils;

/**
 * Base class for all Module level components.
 */
public abstract class AbstractModuleComponent implements ModuleComponent {

    private Module _module;

    public AbstractModuleComponent(Module module) {
        _module = module;
    }

    /**
     * {@inheritDoc}
     */
    public void projectOpened() {
    }

    /**
     * {@inheritDoc}
     */
    public void projectClosed() {
    }

    /**
     * {@inheritDoc}
     */
    public void moduleAdded() {
    }

    /**
     * {@inheritDoc}
     */
    public void initComponent() {
    }

    /**
     * {@inheritDoc}
     */
    public void disposeComponent() {
        _module = null;
    }

    /**
     * Returns the module associated with this component.
     *
     * @return the module associated with this component.
     * @throws NotTapestryModuleException if the module is not a Tapestry module.
     */
    protected Module getModule() throws NotTapestryModuleException {
        if (!TapestryUtils.isTapestryModule(_module)) {
            throw new NotTapestryModuleException();
        }

        return _module;
    }
}//AbstractModuleComponent
