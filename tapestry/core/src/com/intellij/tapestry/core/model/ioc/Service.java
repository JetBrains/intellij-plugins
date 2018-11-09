package com.intellij.tapestry.core.model.ioc;

import com.intellij.tapestry.core.ioc.ServiceBinding;
import com.intellij.tapestry.core.java.IJavaClassType;

/**
 * A Tapestry IoC service.
 */
public class Service {

    private final String _id;
    private final String _scope;
    private final boolean _eagerLoad;
    private final IJavaClassType _serviceClass;
    private final String _description;

    public Service(ServiceBinding serviceBinding, IJavaClassType serviceClass) {
        _id = serviceBinding.getId();
        _scope = serviceBinding.getScope();
        _eagerLoad = serviceBinding.isEagerLoad();

        _serviceClass = serviceClass;
        _description = "";
    }

    public String getId() {
        return _id;
    }

    public String getScope() {
        return _scope;
    }

    public boolean isEagerLoad() {
        return _eagerLoad;
    }

    /**
     * Returns the service description.
     *
     * @return the service description.
     */
    public String getDescription() {
        if (_description != null) {
            return _description;
        }

        return getServiceClass().getDocumentation();
    }

    public IJavaClassType getServiceClass() {
        return _serviceClass;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return _id;
    }
}
