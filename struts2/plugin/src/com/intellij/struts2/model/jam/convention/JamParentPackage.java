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
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementRef;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.model.jam.StrutsJamUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * {@code @org.apache.struts2.convention.annotation.ParentPackage}.
 *
 * @author Yann C&eacute;bron
 */
public final class JamParentPackage extends JamCommonModelElement<PsiModifierListOwner> implements JamElement {
  @NonNls
  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.ParentPackage";

  private static final JamConverter<StrutsPackage> STRUTS_PACKAGE_JAM_CONVERTER =

    new JamSimpleReferenceConverter<>() {

      private final Condition<StrutsPackage> EXTENDABLE_STRUTS_PACKAGE_CONDITION =
        strutsPackage -> StringUtil.isNotEmpty(strutsPackage.getName().getStringValue()) &&
                         StringUtil.isNotEmpty(strutsPackage.getNamespace().getStringValue());

      @Override
      public StrutsPackage fromString(@Nullable final String s, final JamStringAttributeElement<StrutsPackage> context) {
        if (s == null) {
          return null;
        }

        final StrutsModel strutsModel = StrutsJamUtils.getStrutsModel(context);
        if (strutsModel == null) {
          return null;
        }

        return ContainerUtil.find(strutsModel.getStrutsPackages(),
                                  strutsPackage -> Objects.equals(strutsPackage.getName().getStringValue(), s));
      }

      @Override
      public Collection<StrutsPackage> getVariants(final JamStringAttributeElement<StrutsPackage> context) {
        final StrutsModel strutsModel = StrutsJamUtils.getStrutsModel(context);
        if (strutsModel == null) {
          return Collections.emptyList();
        }

        return ContainerUtil.findAll(strutsModel.getStrutsPackages(), EXTENDABLE_STRUTS_PACKAGE_CONDITION);
      }

      @NotNull
      @Override
      protected LookupElement createLookupElementFor(@NotNull final StrutsPackage target) {
        return LookupElementBuilder.create(StringUtil.notNullize(target.getName().getStringValue()))
          .withIcon(StrutsIcons.STRUTS_PACKAGE)
          .withTailText(" (" + target.searchNamespace() + ")", true)
          .withTypeText(DomUtil.getFile(target).getName());
      }
    };

  private static final JamStringAttributeMeta.Single<StrutsPackage> VALUE_ATTRIBUTE =
    JamAttributeMeta.singleString("value", STRUTS_PACKAGE_JAM_CONVERTER);

  private static final JamAnnotationMeta PARENT_PACKAGE_META =
    new JamAnnotationMeta(ANNOTATION_NAME).addAttribute(VALUE_ATTRIBUTE);

  public static final JamClassMeta<JamParentPackage> META_CLASS =
    new JamClassMeta<>(JamParentPackage.class, JamParentPackage::new).addAnnotation(PARENT_PACKAGE_META);

  public static final JamPackageMeta<JamParentPackage> META_PACKAGE =
    new JamPackageMeta<>(JamParentPackage.class, JamParentPackage::new)
      .addAnnotation(PARENT_PACKAGE_META);

  private JamParentPackage(PsiElementRef<?> ref) {
    super(ref);
  }

  /**
   * Returns "value" attribute.
   *
   * @return JAM-Attribute.
   */
  public JamStringAttributeElement<StrutsPackage> getValue() {
    return PARENT_PACKAGE_META.getAttribute(getPsiElement(), VALUE_ATTRIBUTE);
  }
}
