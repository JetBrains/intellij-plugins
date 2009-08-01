/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.dom.struts.constant;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import com.intellij.util.xml.converters.values.BooleanValueConverter;
import com.intellij.util.xml.converters.values.NumberValueConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Yann C&eacute;bron
 */
public class ConstantValueConverterImpl extends ConstantValueConverter {

  private static final Map<String, Converter> CONVERTERS = new HashMap<String, Converter>();

  static {
    addClassWithShortcutProperty("struts.configuration", "", Collections.<String, String>emptyMap());
    addStringProperty("struts.i18n.encoding"); // TODO

    @NonNls final Map<String, String> objectFactoryShortcutMap = new HashMap<String, String>();
    objectFactoryShortcutMap.put("struts", "org.apache.struts2.impl.StrutsObjectFactory");
    objectFactoryShortcutMap.put("spring", "org.apache.struts2.spring.StrutsSpringObjectFactory");
    addClassWithShortcutProperty("struts.objectFactory",
                                 "com.opensymphony.xwork2.ObjectFactory",
                                 objectFactoryShortcutMap);

    addStringValuesProperty("struts.objectFactory.spring.autoWire", "name", "type", "auto", "constructor");
    addBooleanProperty("struts.objectFactory.spring.useClassCache");

    @NonNls final Map<String, String> objectDeterminerShortcutMap = new HashMap<String, String>();
    objectDeterminerShortcutMap.put("tiger", "com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer");
    objectDeterminerShortcutMap.put("notiger", "com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer");
    objectDeterminerShortcutMap.put("struts", "com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer");
    addClassWithShortcutProperty("struts.objectTypeDeterminer",
                                 "com.opensymphony.xwork2.util.ObjectTypeDeterminer",
                                 objectDeterminerShortcutMap);

    addStringValuesProperty("struts.multipart.parser", "cos", "pell", "jakarta");
    addStringProperty("struts.multipart.saveDir");
    addIntegerProperty("struts.multipart.maxSize");
    addStringProperty("struts.custom.properties");
    addClassWithShortcutProperty("struts.mapper.class", "", Collections.<String, String>emptyMap());
    addStringProperty("struts.action.extension");
    addBooleanProperty("struts.serve.static");
    addBooleanProperty("struts.serve.static.browserCache");
    addBooleanProperty("struts.enable.DynamicMethodInvocation");
    addBooleanProperty("struts.enable.SlashesInActionNames");
    addBooleanProperty("struts.tag.altSyntax");
    addBooleanProperty("struts.devMode");
    addBooleanProperty("struts.i18n.reload");
    addStringValuesProperty("struts.ui.theme", "simple", "xhtml", "ajax");
    addStringProperty("struts.ui.templateDir");
    addStringValuesProperty("struts.ui.templateSuffix", "ftl", "vm", "jsp");
    addBooleanProperty("struts.configuration.xml.reload");
    addStringProperty("struts.velocity.configfile");
    addStringProperty("struts.velocity.contexts");
    addStringProperty("struts.velocity.toolboxlocation");
    addIntegerProperty("struts.url.http.port");
    addIntegerProperty("struts.url.https.port");
    addStringValuesProperty("struts.url.includeParams", "none", "get", "all");
    addStringProperty("struts.custom.i18n.resources");
    addBooleanProperty("struts.dispatcher.parametersWorkaround");
    addClassWithShortcutProperty("struts.freemarker.manager.classname",
                                 "org.apache.struts2.views.freemarker.FreemarkerManager",
                                 Collections.<String, String>emptyMap());
    addBooleanProperty("struts.freemarker.templatesCache");
    addBooleanProperty("struts.freemarker.wrapper.altMap");
    addBooleanProperty("struts.xslt.nocache");
    addStringProperty("struts.configuration.files");
    addBooleanProperty("struts.mapper.alwaysSelectFullNamespace");
    addClassWithShortcutProperty("struts.unknownHandlerManager",
                                 "com.opensymphony.xwork2.UnknownHandlerManager",
                                 Collections.<String, String>emptyMap());
  }

  @Nullable
  public Converter<?> getConverter(@NotNull final GenericDomValue domElement) {
    final Constant constant = (Constant) domElement.getParent();
    assert constant != null;
    return CONVERTERS.get(constant.getName().getStringValue());
  }

  public static Set<String> getConstantNames() {
    return CONVERTERS.keySet();
  }

  private static void addStringProperty(@NonNls final String propertyName) {
    CONVERTERS.put(propertyName, EMPTY_CONVERTER);
  }

  private static void addStringValuesProperty(@NonNls final String propertyName, @NonNls final String... values) {
    CONVERTERS.put(propertyName, new StringValuesConverter(values));
  }

  private static void addClassWithShortcutProperty(@NonNls final String propertyName,
                                                   @NonNls final String baseClass,
                                                   final Map<String, String> shortCutToPsiClassMap) {
    CONVERTERS.put(propertyName, new ConstantValueClassConverter(baseClass, shortCutToPsiClassMap));
  }

  private static void addBooleanProperty(@NonNls final String propertyName) {
    CONVERTERS.put(propertyName, new BooleanValueConverter(false));
  }

  private static void addIntegerProperty(@NonNls final String propertyName) {
    CONVERTERS.put(propertyName, new NumberValueConverter(Integer.class, false));
  }


  /**
   * Resolves to list of given names.
   */
  private static class StringValuesConverter extends ResolvingConverter.StringConverter {

    private final String[] values;

    private StringValuesConverter(@NonNls final String... values) {
      Arrays.sort(values);
      this.values = values;
    }

    public String fromString(final String s, final ConvertContext context) {
      return Arrays.binarySearch(values, s) > -1 ? s : null;
    }

    @NotNull
    public Collection<? extends String> getVariants(final ConvertContext context) {
      return Arrays.asList(values);
    }
  }


}