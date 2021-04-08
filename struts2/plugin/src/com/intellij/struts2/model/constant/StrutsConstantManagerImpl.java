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

package com.intellij.struts2.model.constant;

import com.intellij.javaee.model.CommonParamValue;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.model.xml.Filter;
import com.intellij.javaee.web.model.xml.WebApp;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesImplUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.constant.Constant;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.CommonProcessors;
import com.intellij.util.FilteringProcessor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.impl.ConvertContextFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerImpl extends StrutsConstantManager {

  @NonNls
  private static final String STRUTS_DEFAULT_PROPERTIES = "/org/apache/struts2/default.properties";

  @NonNls
  private static final String STRUTS_PROPERTIES_FILENAME = "struts.properties";

  private static final Condition<Filter> WEB_XML_STRUTS_FILTER_CONDITION = filter -> {
    final PsiClass filterClass = filter.getFilterClass().getValue();
    return InheritanceUtil.isInheritor(filterClass, StrutsConstants.STRUTS_2_0_FILTER_CLASS) ||
           InheritanceUtil.isInheritor(filterClass, StrutsConstants.STRUTS_2_1_FILTER_CLASS);
  };


  @NotNull
  @Override
  public List<StrutsConstant> getConstants(@NotNull final Module module) {
    return ContainerUtil.concat(
      EP_NAME.getExtensionList(),
        contributor -> {
          if (!contributor.isAvailable(module)) {
            return Collections.emptyList();
          }

          return contributor.getStrutsConstantDefinitions(module);
        });
  }

  @Override
  @Nullable
  public <T> Converter<T> findConverter(@NotNull final PsiElement context,
                                        @NotNull final StrutsConstantKey<T> strutsConstantKey) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(context);
    if (module == null) {
      return null;
    }

    final StrutsConstant strutsConstant = ContainerUtil.find(getConstants(module), strutsConstant1 -> Objects.equals(
      strutsConstant1.getName(), strutsConstantKey.getKey()));

    //noinspection unchecked
    return strutsConstant != null ? strutsConstant.getConverter() : null;
  }

  @Override
  @Nullable
  public <T> T getConvertedValue(@NotNull final PsiElement context,
                                 @NotNull final StrutsConstantKey<T> strutsConstantKey) {
    final PsiFile containingFile = context.getContainingFile();
    final StrutsModel strutsModel = getStrutsModel(containingFile);
    if (strutsModel == null) {
      return null;
    }

    final String stringValue = getStringValue(containingFile,
                                              strutsModel,
                                              strutsConstantKey.getKey());
    if (stringValue == null) {
      return null;
    }

    final Converter<T> converter = findConverter(context, strutsConstantKey);
    if (converter == null) {
      return (T) stringValue;
    }

    final DomFileElement<StrutsRoot> first = strutsModel.getRoots().iterator().next();

    final ConvertContext convertContext = ConvertContextFactory.createConvertContext(first);
    return converter.fromString(stringValue, convertContext);
  }

  /**
   * Returns the plain String value for the given constant.
   *
   * @param context     Current context.
   * @param strutsModel StrutsModel.
   * @param name        Name of constant.
   * @return {@code null} if no value could be resolved.
   */
  @Nullable
  private static String getStringValue(@NotNull final PsiFile context,
                                       @NotNull final StrutsModel strutsModel,
                                       @NotNull @NonNls final String name) {
    final Project project = context.getProject();
    final Module module = ModuleUtilCore.findModuleForPsiElement(context);
    assert module != null : context;

    // collect all properties with matching key
    final List<IProperty> properties = PropertiesImplUtil.findPropertiesByKey(project, name);

    String value = null;

    // 1. default.properties from struts2-core.jar
    final IProperty strutsDefaultProperty = ContainerUtil.find(properties, property -> {
      final VirtualFile virtualFile = property.getPropertiesFile().getVirtualFile();
      return virtualFile != null &&
             virtualFile.getFileSystem() instanceof JarFileSystem &&
             StringUtil.endsWith(virtualFile.getPath(), STRUTS_DEFAULT_PROPERTIES) &&
             ModuleUtilCore.moduleContainsFile(module, virtualFile, true);
    });
    if (strutsDefaultProperty != null) {
      value = strutsDefaultProperty.getValue();
    }

    // 2. <constant> from StrutsModel
    final Condition<Constant> constantNameCondition = constant -> Objects.equals(constant.getName().getStringValue(), name);

    final List<DomFileElement<StrutsRoot>> domFileElements = new ArrayList<>();
    collectStrutsXmls(domFileElements, strutsModel, "struts-default.xml", true);
    collectStrutsXmls(domFileElements, strutsModel, "struts-plugin.xml", true);
    collectStrutsXmls(domFileElements, strutsModel, "struts.xml", false);
    for (final DomFileElement<StrutsRoot> domFileElement : domFileElements) {
      final Constant constant = ContainerUtil.find(domFileElement.getRootElement().getConstants(),
                                                   constantNameCondition);
      final String strutsXmlValue = constant != null ? constant.getValue().getStringValue() : null;
      if (strutsXmlValue != null) {
        value = strutsXmlValue;
      }
    }

    // 3. struts.properties in current module
    final IProperty strutsProperty = ContainerUtil.find(properties, property -> {
      final VirtualFile virtualFile = property.getPropertiesFile().getVirtualFile();
      return virtualFile != null &&
             Objects.equals(virtualFile.getName(), STRUTS_PROPERTIES_FILENAME) &&
             ModuleUtilCore.moduleContainsFile(module, virtualFile, false);
    });
    if (strutsProperty != null) {
      value = strutsProperty.getValue();
    }

    // 4. web.xml
    final WebFacet webFacet = WebUtil.getWebFacet(context);
    if (webFacet == null) {
      return value; // should not happen in real projects..
    }
    final WebApp webApp = webFacet.getRoot();
    if (webApp == null) {
      return value; // no web.xml
    }

    final Filter filter = ContainerUtil.find(webApp.getFilters(), WEB_XML_STRUTS_FILTER_CONDITION);
    if (filter != null) {
      final CommonParamValue initParam = ContainerUtil.find(filter.getInitParams(),
                                                            (Condition<CommonParamValue>)paramValue -> Objects.equals(
                                                        paramValue.getParamName().getStringValue(), name));
      if (initParam != null) {
        value = initParam.getParamValue().getStringValue();
      }
    }

    return value;
  }

  /**
   * Determines best matching StrutsModel.
   *
   * @param psiFile Context file.
   * @return {@code null} if no model could be determined.
   */
  @Nullable
  private static StrutsModel getStrutsModel(final PsiFile psiFile) {
    final StrutsManager strutsManager = StrutsManager.getInstance(psiFile.getProject());
    final StrutsModel model;
    if (psiFile instanceof XmlFile &&
        strutsManager.isStruts2ConfigFile((XmlFile) psiFile)) {
      model = strutsManager.getModelByFile((XmlFile) psiFile);
    } else {
      model = strutsManager.getCombinedModel(psiFile);
    }
    return model;
  }

  /**
   * Adds all struts.xml files matching the given filename.
   *
   * @param domFileElements Elements to add to.
   * @param model           StrutsModel to search for matching struts.xml.
   * @param strutsXmlName   Name to match.
   * @param onlyInJARs      Only include struts.xml files located in JAR files.
   */
  private static void collectStrutsXmls(@NotNull final List<DomFileElement<StrutsRoot>> domFileElements,
                                        @NotNull final StrutsModel model,
                                        @NotNull @NonNls final String strutsXmlName,
                                        final boolean onlyInJARs) {
    ContainerUtil.process(model.getRoots(),
                          new FilteringProcessor<>(
                            getStrutsXmlCondition(strutsXmlName, onlyInJARs),
                            new CommonProcessors.CollectProcessor<>(domFileElements)));
  }

  /**
   * Returns matcher condition.
   *
   * @param strutsXmlName Filename to match.
   * @param onlyInJARs    Only include struts.xml files located in JAR files.
   * @return Condition.
   */
  private static Condition<DomFileElement<StrutsRoot>> getStrutsXmlCondition(final String strutsXmlName,
                                                                             final boolean onlyInJARs) {
    return strutsRootDomFileElement -> {
      final XmlFile xmlFile = strutsRootDomFileElement.getFile();
      final boolean nameMatch = Objects.equals(xmlFile.getName(), strutsXmlName);
      if (!onlyInJARs) {
        return nameMatch;
      }

      final VirtualFile virtualFile = xmlFile.getVirtualFile();
      return nameMatch &&
             virtualFile != null &&
             virtualFile.getFileSystem() instanceof JarFileSystem;
    };
  }
}
