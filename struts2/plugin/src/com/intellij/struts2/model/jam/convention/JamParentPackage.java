/*
 * Copyright 2011 The authors
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
import com.intellij.jam.JamConverter;
import com.intellij.jam.JamElement;
import com.intellij.jam.JamSimpleReferenceConverter;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.annotations.JamPsiValidity;
import com.intellij.jam.reflect.*;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
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

/**
 * {@code @org.apache.struts2.convention.annotation.ParentPackage}.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class JamParentPackage extends CommonModelElement.PsiBase implements JamElement {

  @NonNls
  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.ParentPackage";

  private static final JamConverter<StrutsPackage> STRUTS_PACKAGE_JAM_CONVERTER =

    new JamSimpleReferenceConverter<StrutsPackage>() {

      private final Condition<StrutsPackage> EXTENDABLE_STRUTS_PACKAGE_CONDITION = new Condition<StrutsPackage>() {
        public boolean value(final StrutsPackage strutsPackage) {
          return StringUtil.isNotEmpty(strutsPackage.getName().getStringValue()) &&
                 StringUtil.isNotEmpty(strutsPackage.getNamespace().getStringValue());
        }
      };

      @Override
      public StrutsPackage fromString(@Nullable final String s, final JamStringAttributeElement<StrutsPackage> context) {
        if (s == null) {
          return null;
        }

        final StrutsModel strutsModel = StrutsJamUtils.getStrutsModel(context);
        if (strutsModel == null) {
          return null;
        }

        return ContainerUtil.find(strutsModel.getStrutsPackages(), new Condition<StrutsPackage>() {
          public boolean value(final StrutsPackage strutsPackage) {
            return Comparing.equal(strutsPackage.getName().getStringValue(), s);
          }
        });
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
          .setIcon(StrutsIcons.PACKAGE)
          .setTailText(" (" + target.searchNamespace() + ")", true)
          .setTypeText(DomUtil.getFile(target).getName());
      }
    };

  private static final JamStringAttributeMeta.Single<StrutsPackage> VALUE_ATTRIBUTE =
    JamAttributeMeta.singleString("value", STRUTS_PACKAGE_JAM_CONVERTER);

  private static final JamAnnotationMeta PARENT_PACKAGE_META =
    new JamAnnotationMeta(ANNOTATION_NAME).addAttribute(VALUE_ATTRIBUTE);

  public static final JamClassMeta<JamParentPackage> META_CLASS =
    new JamClassMeta<JamParentPackage>(JamParentPackage.class).addAnnotation(PARENT_PACKAGE_META);

  public static final JamPackageMeta<JamParentPackage> META_PACKAGE =
    new JamPackageMeta<JamParentPackage>(null, JamParentPackage.class)
      .addAnnotation(PARENT_PACKAGE_META);

  @JamPsiConnector
  public abstract PsiModifierListOwner getOwner();

  @JamPsiValidity
  public abstract boolean isPsiValid();

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
  public JamStringAttributeElement<StrutsPackage> getValue() {
    return PARENT_PACKAGE_META.getAttribute(getOwner(), VALUE_ATTRIBUTE);
  }

}