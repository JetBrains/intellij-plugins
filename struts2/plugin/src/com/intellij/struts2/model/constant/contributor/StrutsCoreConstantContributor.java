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

package com.intellij.struts2.model.constant.contributor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.model.constant.StrutsConstant;
import com.intellij.struts2.model.constant.StrutsConstantKey;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Struts 2 core constants.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsCoreConstantContributor extends StrutsConstantContributorBase {

  /** {@code struts.action.extension}. */
  public static final StrutsConstantKey<String> ACTION_EXTENSION = StrutsConstantKey.create("struts.action.extension");

  @NonNls
  private static final List<StrutsConstant> CONSTANTS = Arrays.asList(
      addClassWithShortcutProperty("struts.configuration", ""),
      addStringValuesProperty("struts.i18n.encoding",
                              ContainerUtil.map2Array(CharsetToolkit.getAvailableCharsets(), String.class,
                                                      new Function<Charset, String>() {
                                                        public String fun(final Charset charset) {
                                                          return charset.name();
                                                        }
                                                      })),

      addClassWithShortcutProperty("struts.objectFactory",
                                   "com.opensymphony.xwork2.ObjectFactory",
                                   Pair.create("struts", "org.apache.struts2.impl.StrutsObjectFactory"),
                                   Pair.create("spring", StrutsConstants.SPRING_OBJECT_FACTORY_CLASS)),

      addStringValuesProperty("struts.objectFactory.spring.autoWire", "name", "type", "auto", "constructor"),
      addBooleanProperty("struts.objectFactory.spring.autoWire.alwaysRespect"),
      addBooleanProperty("struts.objectFactory.spring.useClassCache"),

      addClassWithShortcutProperty("struts.objectTypeDeterminer",
                                   "com.opensymphony.xwork2.util.ObjectTypeDeterminer",
                                   Pair.create("tiger",
                                               "com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer"),
                                   Pair.create("notiger",
                                               "com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer"),
                                   Pair.create("struts",
                                               "com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer")),

      addStringValuesProperty("struts.multipart.parser", "cos", "pell", "jakarta"),
      addStringProperty("struts.multipart.saveDir"),
      addIntegerProperty("struts.multipart.maxSize"),
      addStringProperty("struts.custom.properties"),
      addClassWithShortcutProperty("struts.mapper.class", ""),
      addStringProperty(ACTION_EXTENSION.getKey()),
      addBooleanProperty("struts.serve.static"),
      addBooleanProperty("struts.serve.static.browserCache"),
      addBooleanProperty("struts.enable.DynamicMethodInvocation"),
      addBooleanProperty("struts.enable.SlashesInActionNames"),
      addBooleanProperty("struts.tag.altSyntax"),
      addBooleanProperty("struts.devMode"),
      addBooleanProperty("struts.i18n.reload"),
      addStringValuesProperty("struts.ui.theme", "simple", "xhtml", "ajax"),
      addStringProperty("struts.ui.templateDir"),
      addStringValuesProperty("struts.ui.templateSuffix", "ftl", "vm", "jsp", "java"),
      addBooleanProperty("struts.configuration.xml.reload"),
      addStringProperty("struts.velocity.configfile"),
      addStringProperty("struts.velocity.contexts"),
      addStringProperty("struts.velocity.toolboxlocation"),
      addIntegerProperty("struts.url.http.port"),
      addIntegerProperty("struts.url.https.port"),
      addStringValuesProperty("struts.url.includeParams", "none", "get", "all"),
      addStringProperty("struts.custom.i18n.resources"),
      addBooleanProperty("struts.dispatcher.parametersWorkaround"),
      addClassWithShortcutProperty("struts.freemarker.manager.classname",
                                   "org.apache.struts2.views.freemarker.FreemarkerManager"),
      addBooleanProperty("struts.freemarker.templatesCache"),
      addBooleanProperty("struts.freemarker.wrapper.altMap"),
      addBooleanProperty("struts.xslt.nocache"),
      addStringProperty("struts.configuration.files"),
      addBooleanProperty("struts.mapper.alwaysSelectFullNamespace"),
      addClassWithShortcutProperty("struts.unknownHandlerManager",
                                   "com.opensymphony.xwork2.UnknownHandlerManager"),
      addBooleanProperty("struts.ognl.allowStaticMethodAccess")
  );

  @NotNull
  @Override
  protected String getRequiredPluginClassName() {
    return "core"; // isAvailable() returns always true
  }

  public boolean isAvailable(@NotNull final Module module) {
    return true;
  }

  @NotNull
  public List<StrutsConstant> getStrutsConstantDefinitions(@NotNull final Module module) {
    return CONSTANTS;
  }

}