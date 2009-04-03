package com.intellij.tapestry.core.ioc;

import com.intellij.tapestry.core.java.IJavaClassType;

/**
 * A service binding that is done with service autobuilding.
 */
public class ServiceBinding {

    private IJavaClassType _serviceClass;
    private boolean _eagerLoad = false;
    private String _scope;
    private String _id;

    public ServiceBinding() {
    }

    public IJavaClassType getServiceClass() {
        return _serviceClass;
    }

    public void setServiceClass(IJavaClassType serviceClass) {
        _serviceClass = serviceClass;
    }

    public boolean isEagerLoad() {
        return _eagerLoad;
    }

    public void setEagerLoad(boolean eagerLoad) {
        _eagerLoad = eagerLoad;
    }

    public String getScope() {
        return _scope;
    }

    public void setScope(String scope) {
        _scope = scope;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }
}
