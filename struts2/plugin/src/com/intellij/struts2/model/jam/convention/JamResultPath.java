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

package com.intellij.struts2.model.jam.convention;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.jam.*;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.*;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * {@code org.apache.struts2.convention.annotation.ResultPath}.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class JamResultPath extends CommonModelElement.PsiBase implements JamElement {

  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.ResultPath";

  /**
   * Resolves to property in {@code struts.properties}.
   */
  private static final JamConverter<Property> PROPERTY_CONVERTER = new JamSimpleReferenceConverter<Property>() {

    private Collection<Property> getStrutsProperties(final JamAttributeElement context) {
      final Module module = ModuleUtil.findModuleForPsiElement(context.getPsiElement());
      if (module == null) {
        return Collections.emptyList();
      }

      final PropertiesFile strutsPropertiesFile = PropertiesUtil.getPropertiesFile("struts", module, null);
      return strutsPropertiesFile != null ? strutsPropertiesFile.getProperties() : Collections.<Property>emptyList();
    }

    @Override
    public Property fromString(@Nullable final String s, final JamStringAttributeElement<Property> context) {
      if (s == null) {
        return null;
      }

      final Collection<Property> properties = getStrutsProperties(context);
      return ContainerUtil.find(properties, new Condition<Property>() {
        public boolean value(final Property property) {
          return Comparing.equal(property.getName(), s);
        }
      });
    }

    @Override
    public Collection<Property> getVariants(final JamStringAttributeElement<Property> context) {
      return getStrutsProperties(context);
    }

    @NotNull
    @Override
    protected LookupElement createLookupElementFor(@NotNull final Property target) {
      return LookupElementBuilder.create(target)
        .setIcon(ElementPresentationManager.getIcon(target))
        .setTailText("=" + target.getValue(), true);
    }
  };


  /**
   * Resolves to directory in web base path(s).
   */
  private static JamConverter<WebDirectoryElement> VALUE_CONVERTER = new JamSimpleReferenceConverter<WebDirectoryElement>() {

    @Override
    public WebDirectoryElement fromString(@Nullable final String s,
                                          final JamStringAttributeElement<WebDirectoryElement> context) {
      if (s == null) {
        return null;
      }

      final WebFacet webFacet = WebUtil.getWebFacet(context.getPsiElement());
      if (webFacet == null) {
        return null;
      }

      final WebDirectoryElement webDirectoryElement = WebUtil.getWebUtil().findWebDirectoryElement(s, webFacet);
      if (webDirectoryElement == null) {
        return null;
      }

      return webDirectoryElement.isDirectory() ? webDirectoryElement : null;
    }

  };


  private static final JamStringAttributeMeta.Single<WebDirectoryElement> VALUE_ATTRIBUTE =
    JamAttributeMeta.singleString("value", VALUE_CONVERTER);

  private static final JamStringAttributeMeta.Single<Property> PROPERTY_ATTRIBUTE =
    JamAttributeMeta.singleString("property", PROPERTY_CONVERTER);

  private static final JamAnnotationMeta RESULT_PATH_META =
    new JamAnnotationMeta(ANNOTATION_NAME)
      .addAttribute(VALUE_ATTRIBUTE)
      .addAttribute(PROPERTY_ATTRIBUTE);

  public static final JamClassMeta<JamResultPath> META_CLASS =
    new JamClassMeta<JamResultPath>(JamResultPath.class).addAnnotation(RESULT_PATH_META);

  public static final JamPackageMeta<JamResultPath> META_PACKAGE =
    new JamPackageMeta<JamResultPath>(null, JamResultPath.class)
      .addAnnotation(RESULT_PATH_META);

  @JamPsiConnector
  public abstract PsiModifierListOwner getOwner();

  @NotNull
  @Override
  public PsiElement getPsiElement() {
    return getOwner();
  }

  /**
   * Returns "value" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<WebDirectoryElement> getValue() {
    return RESULT_PATH_META.getAttribute(getOwner(), VALUE_ATTRIBUTE);
  }

  /**
   * Returns "property" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<Property> getProperty() {
    return RESULT_PATH_META.getAttribute(getOwner(), PROPERTY_ATTRIBUTE);
  }

}