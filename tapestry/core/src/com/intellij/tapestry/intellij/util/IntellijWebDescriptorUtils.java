package com.intellij.tapestry.intellij.util;

import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.javaee.model.xml.web.Filter;
import com.intellij.javaee.model.xml.web.FilterMapping;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.util.WebDescriptorUtils;

/**
 * Utility methods for manipulating the web.xml file.
 */
public class IntellijWebDescriptorUtils {

    /**
     * Finds the Tapestry filter name.
     *
     * @param app the web application.
     * @return the defined Tapestry filter, {@code null} if it's not found.
     */
    public static Filter getTapestryFilter(WebApp app) {
        if (app == null)
            return null;

        for (Filter filter : app.getFilters()) {

            if (filter.getFilterClass().getStringValue().equals(TapestryConstants.FILTER_CLASS)) {
                return filter;
            }
        }

        return null;
    }

    /**
     * Finds the defined application root package.
     *
     * @param app the web application.
     * @return the defined application root package, {@code null} if it's not found.
     */
    public static String getApplicationPackage(WebApp app) {
        if (app == null)
            return null;

        for (ParamValue param : app.getContextParams()) {
            if (param.getParamName().getValue().equals(WebDescriptorUtils.APPLICATION_PACKAGE_PARAMETER_NAME))
                return param.getParamValue().getValue();
        }

        return null;
    }

    public static void setApplicationPackage(WebApp app, String applicationPackage) {
        if (app == null)
            return;

        String configuredApplicationPackage = getApplicationPackage(app);

        if (configuredApplicationPackage == null) {
            ParamValue newContextParam = app.addContextParam();

            newContextParam.getParamName().setValue(WebDescriptorUtils.APPLICATION_PACKAGE_PARAMETER_NAME);
            newContextParam.getParamValue().setValue(applicationPackage != null ? applicationPackage : "");
        } else {
            for (ParamValue param : app.getContextParams()) {
                if (param.getParamName().getValue().equals(WebDescriptorUtils.APPLICATION_PACKAGE_PARAMETER_NAME))
                    param.getParamValue().setValue(applicationPackage);
            }
        }
    }

    /**
     * Updates the web.xml file so that a correct Tapestry filter is configured.
     *
     * @param app        the web application.
     * @param filterName the filter name to use.
     */
    public static void updateFilter(WebApp app, String filterName) {
        if (app == null)
            return;

        Filter existingFilter = getTapestryFilter(app);
        String existingFilterName = null;

        if (existingFilter != null) {
            existingFilterName = existingFilter.getFilterName().getValue();

            existingFilter.getFilterName().setValue(filterName);
        } else {
            Filter newFilter = app.addFilter();

            newFilter.getFilterName().setValue(filterName);
            newFilter.getFilterClass().setStringValue(TapestryConstants.FILTER_CLASS);
        }

        if (existingFilterName != null) {
            for (FilterMapping filterMapping : app.getFilterMappings()) {
                if (filterMapping.getFilterName().getStringValue().equals(existingFilterName))
                    filterMapping.getFilterName().setStringValue(filterName);
            }
        } else {
            FilterMapping newFilterMapping = app.addFilterMapping();

            newFilterMapping.getFilterName().setStringValue(filterName);
            newFilterMapping.addUrlPattern().setValue("/*");
        }
    }
}
