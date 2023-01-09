/*
 * Copyright 2014 The authors
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
import com.intellij.jam.reflect.*;
import com.intellij.javaee.web.WebDirectoryElement;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesUtilBase;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.references.PropertiesCompletionContributor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElementRef;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * {@code org.apache.struts2.convention.annotation.ResultPath}.
 *
 * @author Yann C&eacute;bron
 */
public class JamResultPath extends JamCommonModelElement<PsiModifierListOwner> implements JamElement {
  @NonNls
  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.ResultPath";

  /**
   * Resolves to property in {@code struts.properties}.
   */
  private static final JamConverter<IProperty> PROPERTY_CONVERTER = new JamSimpleReferenceConverter<>() {

    private Collection<IProperty> getStrutsProperties(final JamAttributeElement context) {
      final PsiAnnotationMemberValue annotationMemberValue = context.getPsiElement();
      if (annotationMemberValue == null) {
        return Collections.emptyList();
      }

      final Module module = ModuleUtilCore.findModuleForPsiElement(annotationMemberValue);
      if (module == null) {
        return Collections.emptyList();
      }

      final PropertiesFile strutsPropertiesFile = PropertiesUtilBase.getPropertiesFile("struts", module, null);
      return strutsPropertiesFile != null ? strutsPropertiesFile.getProperties() : Collections.emptyList();
    }

    @Override
    public IProperty fromString(@Nullable final String s, final JamStringAttributeElement<IProperty> context) {
      if (s == null) {
        return null;
      }

      final Collection<IProperty> properties = getStrutsProperties(context);
      return ContainerUtil.find(properties, property -> Objects.equals(property.getName(), s));
    }

    @Override
    public Collection<IProperty> getVariants(final JamStringAttributeElement<IProperty> context) {
      return getStrutsProperties(context);
    }

    @NotNull
    @Override
    protected LookupElement createLookupElementFor(@NotNull final IProperty target) {
      return LookupElementBuilder.create((PsiNamedElement)target.getPsiElement())
        .withIcon(ElementPresentationManager.getIcon(target))
        .withTailText("=" + target.getValue(), true).withRenderer(PropertiesCompletionContributor.LOOKUP_ELEMENT_RENDERER);
    }
  };


  /**
   * Resolves to directory in web base path(s).
   */
  private static final JamConverter<WebDirectoryElement> VALUE_CONVERTER = new JamSimpleReferenceConverter<>() {

    @Override
    public WebDirectoryElement fromString(@Nullable final String s,
                                          final JamStringAttributeElement<WebDirectoryElement> context) {
      if (s == null) {
        return null;
      }

      final PsiAnnotationMemberValue annotationMemberValue = context.getPsiElement();
      if (annotationMemberValue == null) {
        return null;
      }

      final WebFacet webFacet = WebUtil.getWebFacet(annotationMemberValue);
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

  private static final JamStringAttributeMeta.Single<IProperty> PROPERTY_ATTRIBUTE =
      JamAttributeMeta.singleString("property", PROPERTY_CONVERTER);

  private static final JamAnnotationMeta RESULT_PATH_META =
      new JamAnnotationMeta(ANNOTATION_NAME)
          .addAttribute(VALUE_ATTRIBUTE)
          .addAttribute(PROPERTY_ATTRIBUTE);

  public static final JamClassMeta<JamResultPath> META_CLASS =
    new JamClassMeta<>(JamResultPath.class, JamResultPath::new).addAnnotation(RESULT_PATH_META);

  public static final JamPackageMeta<JamResultPath> META_PACKAGE =
    new JamPackageMeta<>(JamResultPath.class, JamResultPath::new)
          .addAnnotation(RESULT_PATH_META);

  public JamResultPath(PsiElementRef<?> ref) {
    super(ref);
  }

  /**
   * Returns "value" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<WebDirectoryElement> getValue() {
    return RESULT_PATH_META.getAttribute(getPsiElement(), VALUE_ATTRIBUTE);
  }

  /**
   * Returns "property" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<IProperty> getProperty() {
    return RESULT_PATH_META.getAttribute(getPsiElement(), PROPERTY_ATTRIBUTE);
  }

}
