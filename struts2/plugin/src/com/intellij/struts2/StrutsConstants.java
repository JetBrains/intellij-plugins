/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2;

import org.jetbrains.annotations.NonNls;

/**
 * Plugin-wide constants.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings("SpellCheckingInspection")
public final class StrutsConstants {

  private StrutsConstants() {
  }

  @NonNls
  public static final String STRUTS_2_0_FILTER_CLASS = "org.apache.struts2.dispatcher.FilterDispatcher";

  @NonNls
  public static final String STRUTS_2_1_FILTER_CLASS = "org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter";

  @NonNls
  public static final String XWORK_ACTION_CLASS = "com.opensymphony.xwork2.Action";

  /**
   * Spring object factory.
   */
  @NonNls
  public static final String SPRING_OBJECT_FACTORY_CLASS = "org.apache.struts2.spring.StrutsSpringObjectFactory";

  /**
   * Default filename for Struts configuration file.
   */
  @NonNls
  public static final String STRUTS_XML_DEFAULT_FILENAME = "struts.xml";

  /**
   * struts.xml 2.0 URI.
   */
  @NonNls
  public static final String STRUTS_2_0_DTD_URI = "http://struts.apache.org/dtds/struts-2.0.dtd";

  /**
   * struts.xml 2.0 ID.
   */
  @NonNls
  public static final String STRUTS_2_0_DTD_ID = "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN";

  /**
   * struts.xml 2.1 URI.
   */
  @NonNls
  public static final String STRUTS_2_1_DTD_URI = "http://struts.apache.org/dtds/struts-2.1.dtd";

  /**
   * struts.xml 2.1 ID.
   */
  @NonNls
  public static final String STRUTS_2_1_DTD_ID = "-//Apache Software Foundation//DTD Struts Configuration 2.1//EN";

  /**
   * struts.xml 2.1.7 URI.
   */
  @NonNls
  public static final String STRUTS_2_1_7_DTD_URI = "http://struts.apache.org/dtds/struts-2.1.7.dtd";

  /**
   * struts.xml 2.1.7 ID.
   */
  @NonNls
  public static final String STRUTS_2_1_7_DTD_ID = "-//Apache Software Foundation//DTD Struts Configuration 2.1.7//EN";

  /**
   * struts.xml 2.3 URI.
   */
  @NonNls
  public static final String STRUTS_2_3_DTD_URI = "http://struts.apache.org/dtds/struts-2.3.dtd";

  /**
   * struts.xml 2.3 ID.
   */
  @NonNls
  public static final String STRUTS_2_3_DTD_ID = "-//Apache Software Foundation//DTD Struts Configuration 2.3//EN";


  @NonNls
  public static final String STRUTS_2_5_DTD_URI = "http://struts.apache.org/dtds/struts-2.5.dtd";
  @NonNls
  public static final String STRUTS_2_5_DTD_ID = "-//Apache Software Foundation//DTD Struts Configuration 2.5//EN";

  /**
   * All struts.xml DTD-IDs/URIs.
   */
  @NonNls
  public static final String[] STRUTS_DTDS = {
    STRUTS_2_0_DTD_URI, STRUTS_2_0_DTD_ID,
    STRUTS_2_1_DTD_URI, STRUTS_2_1_DTD_ID,
    STRUTS_2_1_7_DTD_URI, STRUTS_2_1_7_DTD_ID,
    STRUTS_2_3_DTD_URI, STRUTS_2_3_DTD_ID,
    STRUTS_2_5_DTD_URI, STRUTS_2_5_DTD_ID
  };

  @NonNls
  public static final String VALIDATOR_1_00_DTD_URI = "http://www.opensymphony.com/xwork/xwork-validator-1.0.dtd";
  @NonNls
  public static final String VALIDATOR_1_00_DTD_ID = "-//OpenSymphony Group//XWork Validator 1.0//EN";
  @NonNls
  public static final String VALIDATOR_1_00_STRUTS_DTD_URI = "http://struts.apache.org/dtds/xwork-validator-1.0.dtd";
  @NonNls
  public static final String VALIDATOR_1_00_STRUTS_DTD_ID = "-//Apache Struts//XWork Validator 1.0//EN";

  @NonNls
  public static final String VALIDATOR_1_02_DTD_URI = "http://www.opensymphony.com/xwork/xwork-validator-1.0.2.dtd";
  @NonNls
  public static final String VALIDATOR_1_02_DTD_ID = "-//OpenSymphony Group//XWork Validator 1.0.2//EN";
  @NonNls
  public static final String VALIDATOR_1_02_STRUTS_DTD_URI = "http://struts.apache.org/dtds/xwork-validator-1.0.2.dtd";
  @NonNls
  public static final String VALIDATOR_1_02_STRUTS_DTD_ID = "-//Apache Struts//XWork Validator 1.0.2//EN";

  @NonNls
  public static final String VALIDATOR_1_03_DTD_URI = "http://struts.apache.org/dtds/xwork-validator-1.0.3.dtd";
  @NonNls
  public static final String VALIDATOR_1_03_DTD_ID = "-//Apache Struts//XWork Validator 1.0.3//EN";

  /**
   * All XXX-validation.xml DTD-IDs/URIs.
   */
  @NonNls
  public static final String[] VALIDATOR_DTDS = {
    VALIDATOR_1_00_DTD_URI, VALIDATOR_1_00_DTD_ID,
    VALIDATOR_1_00_STRUTS_DTD_URI, VALIDATOR_1_00_STRUTS_DTD_ID,
    VALIDATOR_1_02_DTD_URI, VALIDATOR_1_02_DTD_ID,
    VALIDATOR_1_02_STRUTS_DTD_URI, VALIDATOR_1_02_STRUTS_DTD_ID,
    VALIDATOR_1_03_DTD_URI, VALIDATOR_1_03_DTD_ID
  };

  @NonNls
  public static final String VALIDATOR_CONFIG_DTD_URI = "http://www.opensymphony.com/xwork/xwork-validator-config-1.0.dtd";
  @NonNls
  public static final String VALIDATOR_CONFIG_DTD_ID = "-//OpenSymphony Group//XWork Validator Config 1.0//EN";
  @NonNls
  public static final String VALIDATOR_CONFIG_STRUTS_DTD_URI = "http://struts.apache.org/dtds/xwork-validator-config-1.0.dtd";
  @NonNls
  public static final String VALIDATOR_CONFIG_STRUTS_DTD_ID = "-//Apache Struts//XWork Validator Config 1.0//EN";


  @NonNls
  public static final String VALIDATOR_DEFINITION_DTD_URI = "http://struts.apache.org/dtds/xwork-validator-definition-1.0.dtd";
  @NonNls
  public static final String VALIDATOR_DEFINITION_DTD_ID = "-//Apache Struts//XWork Validator Definition 1.0//EN";

  @NonNls
  public static final String[] VALIDATOR_CONFIG_DTDS = {
    VALIDATOR_CONFIG_DTD_URI, VALIDATOR_CONFIG_DTD_ID,
    VALIDATOR_CONFIG_STRUTS_DTD_URI, VALIDATOR_CONFIG_STRUTS_DTD_ID,
    VALIDATOR_DEFINITION_DTD_URI, VALIDATOR_DEFINITION_DTD_ID
  };

  /**
   * Struts UI taglib URI.
   */
  @NonNls
  public static final String TAGLIB_STRUTS_UI_URI = "/struts-tags";

  /**
   * Struts UI taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_STRUTS_UI_PREFIX = "s";

  /**
   * Struts UI taglib CSS-attributes.
   */
  @NonNls
  public static final String[] TAGLIB_STRUTS_UI_CSS_ATTRIBUTES =
    new String[]{"buttonCssStyle", "cssErrorStyle", "cssStyle", "doubleCssStyle"};

  /**
   * Struts jQuery-plugin taglib URI.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_PLUGIN_URI = "/struts-jquery-tags";

  /**
   * Struts jQuery-plugin taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_PLUGIN_PREFIX = "sj";

  /**
   * Struts jQuery-plugin richtext taglib URI.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI = "/struts-jquery-richtext-tags";

  /**
   * Struts jQuery-plugin richtext taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_RICHTEXT_PLUGIN_PREFIX = "sjr";

  /**
   * Struts jQuery-plugin chart taglib URI.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_CHART_PLUGIN_URI = "/struts-jquery-chart-tags";

  /**
   * Struts jQuery-plugin chart taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_CHART_PLUGIN_PREFIX = "sjc";

  /**
   * Struts jQuery-plugin tree taglib URI.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_TREE_PLUGIN_URI = "/struts-jquery-tree-tags";

  /**
   * Struts jQuery-plugin tree taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_TREE_PLUGIN_PREFIX = "sjt";

  /**
   * Struts jQuery-plugin grid taglib URI.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_GRID_PLUGIN_URI = "/struts-jquery-grid-tags";

  /**
   * Struts jQuery-plugin grid taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_GRID_PLUGIN_PREFIX = "sjg";

  /**
   * Struts jQuery-plugin mobile taglib URI.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_MOBILE_PLUGIN_URI = "/struts-jquery-mobile-tags";

  /**
   * Struts jQuery-plugin mobile taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_JQUERY_MOBILE_PLUGIN_PREFIX = "sjm";

  /**
   * Struts bootstrap-plugin taglib URI.
   */
  @NonNls
  public static final String TAGLIB_BOOTSTRAP_PLUGIN_URI = "/struts-bootstrap-tags";

  /**
   * Struts bootstrap-plugin taglib default prefix.
   */
  @NonNls
  public static final String TAGLIB_BOOTSTRAP_PLUGIN_PREFIX = "sb";
}