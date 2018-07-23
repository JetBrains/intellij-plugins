package com.intellij.tapestry.core.ioc;

import com.intellij.tapestry.core.java.IJavaMethod;

import java.util.Collection;

/**
 * Finds all service bindings done using autobuilding.
 */
public interface IServiceBindingDiscoverer {

    /**
     * @param method the bind method.
     * @return all service bindings.
     */
    Collection<ServiceBinding> getServiceBindings(IJavaMethod method);
}
