/*
 * Copyright 2012 The authors
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
import com.intellij.jam.JamSimpleReferenceConverter;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.model.jam.StrutsJamUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Reference to Interceptor for JAM.
 *
 * @author Yann C&eacute;bron
 */
class InterceptorJamReferenceConverter extends JamSimpleReferenceConverter<InterceptorOrStackBase> {

  @Override
  public InterceptorOrStackBase fromString(@Nullable final String s,
                                           final JamStringAttributeElement<InterceptorOrStackBase> context) {
    if (s == null) {
      return null;
    }

    final StrutsModel strutsModel = StrutsJamUtils.getStrutsModel(context);
    if (strutsModel == null) {
      return null;
    }

    return ContainerUtil.find(strutsModel.getAllInterceptorsAndStacks(),
                              interceptorOrStackBase -> Comparing.strEqual(s, interceptorOrStackBase.getName().getStringValue()));
  }

  @Override
  public Collection<InterceptorOrStackBase> getVariants(final JamStringAttributeElement<InterceptorOrStackBase> context) {
    final StrutsModel strutsModel = StrutsJamUtils.getStrutsModel(context);
    if (strutsModel == null) {
      return Collections.emptyList();
    }

    return strutsModel.getAllInterceptorsAndStacks();
  }

  @NotNull
  @Override
  protected LookupElement createLookupElementFor(@NotNull final InterceptorOrStackBase target) {
    return LookupElementBuilder.create(StringUtil.notNullize(target.getName().getStringValue()))
                               .withIcon(ElementPresentationManager.getIcon(target))
                               .withTypeText(DomUtil.getFile(target).getName());
  }

}