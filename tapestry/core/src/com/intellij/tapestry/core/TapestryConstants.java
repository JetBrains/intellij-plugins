package com.intellij.tapestry.core;

public interface TapestryConstants {

  /**
   * The Tapestry namespace used in templates.
   */
  String TEMPLATE_NAMESPACE = "http://tapestry.apache.org/schema/tapestry_5_1_0.xsd";
  String TEMPLATE_NAMESPACE2 = "http://tapestry.apache.org/schema/tapestry_5_0_0.xsd";
  String TEMPLATE_NAMESPACE3 = "http://tapestry.apache.org/schema/tapestry_5_3.xsd";
  String TEMPLATE_NAMESPACE4 = "http://tapestry.apache.org/schema/tapestry_5_4.xsd";

  /**
   * The Tapestry namespace used for parameters.
   */
  String PARAMETERS_NAMESPACE = "tapestry:parameter";

  /**
   * Service builder method name prefix.
   */
  String SERVICE_BUILDER_METHOD_PREFIX = "build";

  /**
   * Service autobuilder method name.
   */
  String SERVICE_AUTOBUILDER_METHOD_NAME = "bind";

  /**
   * Regexp of a service builder method.
   */
  String SERVICE_BUILDER_METHOD_REGEXP = SERVICE_BUILDER_METHOD_PREFIX + "[\\w\\$]*";

  /**
   * Name of the template for a new module builder class.
   */
  String MODULE_BUILDER_CLASS_TEMPLATE_NAME = "Tapestry Ioc Module Builder Class.java";

  /**
   * Name of the template for a new module builder class.
   */
  String COMPONENT_CLASS_TEMPLATE_NAME = "Tapestry Component Class.java";

  /**
   * Name of the template for a new component template.
   */
  String COMPONENT_TEMPLATE_TEMPLATE_NAME = "Tapestry Component Template.html";

  /**
   * Name of the template for a new page class.
   */
  String PAGE_CLASS_TEMPLATE_NAME = "Tapestry Page Class.java";

  /**
   * Name of the template for a new page template.
   */
  String PAGE_TEMPLATE_TEMPLATE_NAME = "Tapestry Page Template.html";

  /**
   * Name of the template for a new mixin class.
   */
  String MIXIN_CLASS_TEMPLATE_NAME = "Tapestry Mixin Class.java";

  /**
   * Name of the template for a new start page template.
   */
  String START_PAGE_TEMPLATE_TEMPLATE_NAME = "Tapestry Start Page Template.html";

  /**
   * Name of the template for a new start page class.
   */
  String START_PAGE_CLASS_TEMPLATE_NAME = "Tapestry Start Page Class.java";

  /**
   * Name of the template for a new pom.
   */
  String POM_TEMPLATE_NAME = "Tapestry Project Pom.xml";

  /**
   * Base package for Tapestry pages.
   */
  String PAGES_PACKAGE = "pages";

  /**
   * Base package for Tapestry components.
   */
  String COMPONENTS_PACKAGE = "components";

  /**
   * Base package for Tapestry mixins.
   */
  String MIXINS_PACKAGE = "mixins";

  /**
   * Base package for Tapestry services.
   */
  String SERVICES_PACKAGE = "services";

  /**
   * Base package for Tapestry base classes.
   */
  String BASE_PACKAGE = "base";

  String[] ELEMENT_PACKAGES = {PAGES_PACKAGE, COMPONENTS_PACKAGE, BASE_PACKAGE, MIXINS_PACKAGE};

  /**
   * The suffix of the module builder class.
   */
  String MODULE_BUILDER_SUFIX = "Module";

  /**
   * The extension of template files.
   */
  String TEMPLATE_FILE_EXTENSION = "tml";

  /**
   * The extension of property files.
   */
  String PROPERTIES_FILE_EXTENSION = ".properties";

  /**
   * The base package for the Tapestry core library.
   */
  String CORE_LIBRARY_PACKAGE = "org.apache.tapestry5.corelib";

  /**
   * The base package for the Tapestry ioc library.
   */
  String IOC_LIBRARY_PACKAGE = "org.apache.tapestry5.ioc";

  /**
   * The Inject annotation class in Tapestry core.
   */
  String CORE_INJECT_ANNOTATION = "org.apache.tapestry5.annotations.Inject";

  /**
   * The Property annotation class in Tapestry core.
   */
  String PROPERTY_ANNOTATION = "org.apache.tapestry5.annotations.Property";
  /**
   * The Component annotation class in Tapestry core.
   */
  String COMPONENT_ANNOTATION = "org.apache.tapestry5.annotations.Component";

  /**
   * The Event annotation class in Tapestry core.
   */
  String EVENT_ANNOTATION = "org.apache.tapestry5.annotations.OnEvent";

  /**
   * The inject page annotation class in Tapestry core.
   */
  String INJECT_PAGE_ANNOTATION = "org.apache.tapestry5.annotations.InjectPage";

  /**
   * The mixin annotation class in Tapestry core.
   */
  String MIXIN_ANNOTATION = "org.apache.tapestry5.annotations.Mixin";

  /**
   * Provided module id.
   */
  String BUILTIN_MODULE_ID = "tapestry.ioc";

  /**
   * The Tapestry filter class.
   */
  String FILTER_CLASS = "org.apache.tapestry5.TapestryFilter";

  /**
   * The Tapestry Home page.
   */
  String HOME_PAGE = "Start";

  /**
   * The prefix of the default parameter methods.
   */
  String DEFAULT_PARAMETER_METHOD_PREFIX = "default";
}
