/*
 * Copyright 2009 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import com.intellij.jam.JamElement;
import com.intellij.jam.JamSimpleReferenceConverter;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
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
public abstract class JamParentPackage extends CommonModelElement.PsiBase implements JamElement {

  @NonNls
  public static final String ANNOTATION_NAME = "org.apache.struts2.convention.annotation.ParentPackage";

  private static final JamSimpleReferenceConverter<StrutsPackage> STRUTS_PACKAGE_JAM_CONVERTER =

    new JamSimpleReferenceConverter<StrutsPackage>() {

      @Override
      public StrutsPackage fromString(@Nullable final String s, final JamStringAttributeElement<StrutsPackage> context) {
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

        return ContainerUtil.findAll(strutsModel.getStrutsPackages(), new Condition<StrutsPackage>() {
          public boolean value(final StrutsPackage strutsPackage) {
            return StringUtil.isNotEmpty(strutsPackage.getName().getStringValue()) &&
                   StringUtil.isNotEmpty(strutsPackage.getNamespace().getStringValue());
          }
        });
      }

      @NotNull
      @Override
      protected LookupElement createLookupElementFor(@NotNull final StrutsPackage target) {
        return LookupElementBuilder.create(StringUtil.notNullize(target.getName().getStringValue()))
          .setIcon(StrutsIcons.PACKAGE)
          .setTailText(" (" + StringUtil.notNullize(target.getNamespace().getStringValue()) + ")", true)
          .setTypeText(DomUtil.getFile(target).getName());
      }
    };

  private static final JamStringAttributeMeta.Single<StrutsPackage> VALUE_ATTRIBUTE =
    JamStringAttributeMeta.singleString("value", STRUTS_PACKAGE_JAM_CONVERTER);

  private static final JamAnnotationMeta PARENT_PACKAGE_META = new JamAnnotationMeta(ANNOTATION_NAME).addAttribute(VALUE_ATTRIBUTE);

  public static final JamClassMeta<JamParentPackage> META =
    new JamClassMeta<JamParentPackage>(JamParentPackage.class).addAnnotation(PARENT_PACKAGE_META);

}