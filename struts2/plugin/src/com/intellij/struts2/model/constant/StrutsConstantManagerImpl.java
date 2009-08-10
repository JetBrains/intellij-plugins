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

package com.intellij.struts2.model.constant;

import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.javaee.model.xml.web.Filter;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
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
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsConstantManagerImpl extends StrutsConstantManager {

  @NonNls
  private static final String STRUTS_DEFAULT_PROPERTIES = "/org/apache/struts2/default.properties";

  @NonNls
  private static final String STRUTS_PROPERTIES_FILENAME = "struts.properties";

  private static final Condition<Filter> WEB_XML_STRUTS_FILTER_CONDITION = new Condition<Filter>() {
    public boolean value(final Filter filter) {
      final PsiClass filterClass = filter.getFilterClass().getValue();
      return InheritanceUtil.isInheritor(filterClass, StrutsConstants.STRUTS_2_0_FILTER_CLASS) ||
             InheritanceUtil.isInheritor(filterClass, StrutsConstants.STRUTS_2_1_FILTER_CLASS);
    }
  };


  @NotNull
  @Override
  public List<StrutsConstant> getConstants(@NotNull final Module module) {
    return ContainerUtil.concat(
        Extensions.getExtensions(EP_NAME),
        new Function<StrutsConstantContributor, Collection<? extends StrutsConstant>>() {
          public Collection<? extends StrutsConstant> fun(final StrutsConstantContributor contributor) {
            if (!contributor.isAvailable(module)) {
              return Collections.emptyList();
            }

            return contributor.getStrutsConstantDefinitions(module);
          }
        });
  }

  @Override
  @Nullable
  public StrutsConstant findByName(@NotNull final Module module, @NotNull @NonNls final String name) {
    return ContainerUtil.find(getConstants(module), new Condition<StrutsConstant>() {
      public boolean value(final StrutsConstant strutsConstant) {
        return strutsConstant.getName().equals(name);
      }
    });
  }

  @Override
  @Nullable
  public String getValue(@NotNull final PsiFile context, @NotNull @NonNls final String name) {
    final Project project = context.getProject();
    final Module module = ModuleUtil.findModuleForPsiElement(context);
    final StrutsManager strutsManager = StrutsManager.getInstance(project);

    // determine best matching StrutsModel
    final StrutsModel model;
    if (context instanceof XmlFile &&
        strutsManager.isStruts2ConfigFile((XmlFile) context)) {
      model = strutsManager.getModelByFile((XmlFile) context);
    } else {
      model = strutsManager.getCombinedModel(module);
    }
    assert model != null;

    // collect all properties with matching key
    final List<Property> properties =
        ContainerUtil.findAll(PropertiesUtil.findPropertiesByKey(project, name), new Condition<Property>() {
          public boolean value(final Property property) {
            return Comparing.equal(property.getKey(), name);
          }
        });


    String value = null;

    // 1. default.properties from struts2-core.jar
    final Property strutsDefaultProperty = ContainerUtil.find(properties, new Condition<Property>() {
      public boolean value(final Property property) {
        final VirtualFile virtualFile = property.getContainingFile().getVirtualFile();
        return virtualFile != null &&
               virtualFile.getFileSystem() instanceof JarFileSystem &&
               StringUtil.endsWith(virtualFile.getPath(), STRUTS_DEFAULT_PROPERTIES) &&
               ModuleUtil.moduleContainsFile(module, virtualFile, true);
      }
    });
    if (strutsDefaultProperty != null) {
      value = strutsDefaultProperty.getValue();
    }

    // 2. <constant> from StrutsModel
    final Condition<Constant> constantNameCondition = new Condition<Constant>() {
      public boolean value(final Constant constant) {
        return Comparing.equal(constant.getName().getStringValue(), name);
      }
    };

    final List<DomFileElement<StrutsRoot>> domFileElements = new ArrayList<DomFileElement<StrutsRoot>>();
    collectStrutsXmls(domFileElements, model, "struts-default.xml", true);
    collectStrutsXmls(domFileElements, model, "struts-plugin.xml", true);
    collectStrutsXmls(domFileElements, model, "struts.xml", false);
    for (final DomFileElement<StrutsRoot> domFileElement : domFileElements) {
      final Constant constant = ContainerUtil.find(domFileElement.getRootElement().getConstants(),
                                                   constantNameCondition);
      final String strutsXmlValue = constant != null ? constant.getValue().getStringValue() : null;
      if (strutsXmlValue != null) {
        value = strutsXmlValue;
      }
    }

    // 3. struts.properties in current module
    final Property strutsProperty = ContainerUtil.find(properties, new Condition<Property>() {
      public boolean value(final Property property) {
        final VirtualFile virtualFile = property.getContainingFile().getVirtualFile();
        return virtualFile != null &&
               Comparing.equal(virtualFile.getName(), STRUTS_PROPERTIES_FILENAME) &&
               ModuleUtil.moduleContainsFile(module, virtualFile, false);
      }
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
      return value; // should not happen in real projects..
    }

    final Filter filter = ContainerUtil.find(webApp.getFilters(), WEB_XML_STRUTS_FILTER_CONDITION);
    if (filter != null) {
      final ParamValue initParam = ContainerUtil.find(filter.getInitParams(), new Condition<ParamValue>() {
        public boolean value(final ParamValue paramValue) {
          return Comparing.equal(paramValue.getParamName().getStringValue(), name);
        }
      });
      if (initParam != null) {
        value = initParam.getParamValue().getStringValue();
      }
    }

    return value;
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
    ContainerUtil.process(
        model.getRoots(),
        new FilteringProcessor<DomFileElement<StrutsRoot>>(new Condition<DomFileElement<StrutsRoot>>() {
          public boolean value(final DomFileElement<StrutsRoot> strutsRootDomFileElement) {
            final XmlFile xmlFile = strutsRootDomFileElement.getFile();
            final boolean nameMatch = Comparing.equal(xmlFile.getName(), strutsXmlName);
            if (!onlyInJARs) {
              return nameMatch;
            }

            final VirtualFile virtualFile = xmlFile.getVirtualFile();
            return nameMatch &&
                   virtualFile != null &&
                   virtualFile.getFileSystem() instanceof JarFileSystem;
          }
        }, new CommonProcessors.CollectProcessor<DomFileElement<StrutsRoot>>(domFileElements)));
  }

}