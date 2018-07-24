package com.intellij.tapestry.core.model.ioc;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.ioc.ServiceBinding;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaMethod;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A Tapestry IoC module builder.
 */
public class ModuleBuilder {

    private static final String SCOPE_ANNOTATION = "org.apache.tapestry5.ioc.annotations.Scope";
    private static final String EAGERLOAD_ANNOTATION = "org.apache.tapestry5.ioc.annotations.EagerLoad";
    private final IJavaClassType _moduleBuilderClass;
    private final TapestryProject _project;
    private Collection<Service> _servicesCache;
    private long _servicesCacheTimestamp;

    public ModuleBuilder(IJavaClassType moduleBuilderClass, TapestryProject project) {

        _moduleBuilderClass = moduleBuilderClass;
        _project = project;
    }

    /**
     * Finds all services the module declares.
     *
     * @return all services the module declares.
     */
    public Collection<Service> getServices() {
        if (_servicesCache != null && !hasChanged(_servicesCacheTimestamp)) {
            return _servicesCache;
        }

        _servicesCache = new ArrayList<>();
        for (IJavaMethod method : _moduleBuilderClass.getPublicMethods(true)) {
            if (!(method.getReturnType() instanceof IJavaClassType)) {
                continue;
            }

            // Default service building(build methods)
            if (method.getName().matches(TapestryConstants.SERVICE_BUILDER_METHOD_REGEXP)) {
                _servicesCache.add(getServiceFromBuildMethod(method));
            }

            // Autobuilding
            if (method.getName().equals(TapestryConstants.SERVICE_AUTOBUILDER_METHOD_NAME)) {
                _servicesCache.addAll(getServicesFromBindMethod(method));
            }
        }

        _servicesCacheTimestamp = _moduleBuilderClass.getFile().getFile().lastModified();
        return _servicesCache;
    }

    private Service getServiceFromBuildMethod(IJavaMethod method) {
        ServiceBinding serviceBinding = new ServiceBinding();
        IJavaAnnotation scopeAnnotation = method.getAnnotation(SCOPE_ANNOTATION);
        if (scopeAnnotation != null) {
            serviceBinding.setScope(scopeAnnotation.getParameters().get("value")[0]);
        }

        if (method.getAnnotation(EAGERLOAD_ANNOTATION) != null) {
            serviceBinding.setEagerLoad(true);
        }
        if (method.getName().equals(TapestryConstants.SERVICE_BUILDER_METHOD_PREFIX)) {
            serviceBinding.setId((method.getReturnType()).getName());

            return new Service(serviceBinding, (IJavaClassType) method.getReturnType());
        }

        serviceBinding.setId(method.getName().substring(TapestryConstants.SERVICE_BUILDER_METHOD_PREFIX.length()));
        return new Service(serviceBinding, (IJavaClassType) method.getReturnType());
    }

    private Collection<Service> getServicesFromBindMethod(IJavaMethod method) {
        Collection<Service> services = new ArrayList<>();

        for (ServiceBinding binding : _project.getJavaTypeFinder().getServiceBindingDiscoverer().getServiceBindings(method))
            services.add(new Service(binding, (IJavaClassType) method.getReturnType()));

        return services;
    }

    /**
     * Checks if the module builder class file has changed since a given timestamp.
     *
     * @param timestamp the timestamp to check.
     * @return {@code true} if the file was changed since the given timestamp, {@code false} otherwise.
     */
    private boolean hasChanged(long timestamp) {
        return _moduleBuilderClass.getFile().getFile().lastModified() > timestamp;
    }
}
