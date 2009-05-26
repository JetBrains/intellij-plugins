package com.intellij.tapestry.core;

public interface TapestryConstants {

    /**
     * The Tapestry namespace used in templates.
     */
    public static final String TEMPLATE_NAMESPACE = "http://tapestry.apache.org/schema/tapestry_5_0_0.xsd";

    /**
     * Service builder method name prefix.
     */
    public static final String SERVICE_BUILDER_METHOD_PREFIX = "build";

    /**
     * Service autobuilder method name.
     */
    public static final String SERVICE_AUTOBUILDER_METHOD_NAME = "bind";

    /**
     * Regexp of a service builder method.
     */
    public static final String SERVICE_BUILDER_METHOD_REGEXP = SERVICE_BUILDER_METHOD_PREFIX + "[\\w\\$]*";

    /**
     * Name of the template for a new module builder class.
     */
    public static final String MODULE_BUILDER_CLASS_TEMPLATE_NAME = "Tapestry Ioc Module Builder Class.java";

    /**
     * Name of the template for a new module builder class.
     */
    public static final String COMPONENT_CLASS_TEMPLATE_NAME = "Tapestry Component Class.java";

    /**
     * Name of the template for a new component template.
     */
    public static final String COMPONENT_TEMPLATE_TEMPLATE_NAME = "Tapestry Component Template.html";

    /**
     * Name of the template for a new page class.
     */
    public static final String PAGE_CLASS_TEMPLATE_NAME = "Tapestry Page Class.java";

    /**
     * Name of the template for a new page template.
     */
    public static final String PAGE_TEMPLATE_TEMPLATE_NAME = "Tapestry Page Template.html";

    /**
     * Name of the template for a new mixin class.
     */
    public static final String MIXIN_CLASS_TEMPLATE_NAME = "Tapestry Mixin Class.java";

    /**
     * Name of the template for a new start page template.
     */
    public static final String START_PAGE_TEMPLATE_TEMPLATE_NAME = "Tapestry Start Page Template.html";

    /**
     * Name of the template for a new start page class.
     */
    public static final String START_PAGE_CLASS_TEMPLATE_NAME = "Tapestry Start Page Class.java";

    /**
     * Name of the template for a new pom.
     */
    public static final String POM_TEMPLATE_NAME = "Tapestry Project Pom.xml";

    /**
     * Base package for Tapestry pages.
     */
    public static final String PAGES_PACKAGE = "pages";

    /**
     * Base package for Tapestry components.
     */
    public static final String COMPONENTS_PACKAGE = "components";

    /**
     * Base package for Tapestry mixins.
     */
    public static final String MIXINS_PACKAGE = "mixins";

    /**
     * Base package for Tapestry services.
     */
    public static final String SERVICES_PACKAGE = "services";

    /**
     * Base package for Tapestry base classes.
     */
    public static final String BASE_PACKAGE = "base";

    /**
     * The suffix of the module builder class.
     */
    public static final String MODULE_BUILDER_SUFIX = "Module";

    /**
     * The extension of template files.
     */
    public static final String TEMPLATE_FILE_EXTENSION = "tml";

    /**
     * The extension of property files.
     */
    public static final String PROPERTIES_FILE_EXTENSION = ".properties";

    /**
     * The base package for the Tapestry core library.
     */
    public static final String CORE_LIBRARY_PACKAGE = "org.apache.tapestry.corelib";

    /**
     * The base package for the Tapestry ioc library.
     */
    public static final String IOC_LIBRARY_PACKAGE = "org.apache.tapestry.ioc";

    /**
     * The Inject annotation class in Tapestry core.
     */
    public static final String CORE_INJECT_ANNOTATION = "org.apache.tapestry.annotations.Inject";

    /**
     * The Component annotation class in Tapestry core.
     */
    public static final String COMPONENT_ANNOTATION = "org.apache.tapestry.annotations.Component";

    /**
     * The Event annotation class in Tapestry core.
     */
    public static final String EVENT_ANNOTATION = "org.apache.tapestry.annotations.OnEvent";

    /**
     * The inject page annotation class in Tapestry core.
     */
    public static final String INJECT_PAGE_ANNOTATION = "org.apache.tapestry.annotations.InjectPage";

    /**
     * The mixin annotation class in Tapestry core.
     */
    public static final String MIXIN_ANNOTATION = "org.apache.tapestry.annotations.Mixin";

    /**
     * Provided module id.
     */
    public static final String BUILTIN_MODULE_ID = "tapestry.ioc";

    /**
     * The Tapestry filter class.
     */
    public static final String FILTER_CLASS = "org.apache.tapestry.TapestryFilter";

    /**
     * The Tapestry Home page.
     */
    public static final String HOME_PAGE = "Start";

    /**
     * The prefix of the default parameter methods.
     */
    public static final String DEFAULT_PARAMETER_METHOD_PREFIX = "default";
}
