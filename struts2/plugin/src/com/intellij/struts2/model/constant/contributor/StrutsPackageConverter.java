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

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Configuration property resolving to {@link StrutsPackage}.
 *
 * @author Yann C&eacute;bron
 */
class StrutsPackageConverter extends ResolvingConverter<StrutsPackage> {

  @NotNull
  @Override
  public Collection<? extends StrutsPackage> getVariants(final ConvertContext convertContext) {
    return getStrutsPackages(convertContext);
  }

  @Override
  public StrutsPackage fromString(@Nullable final String s, final ConvertContext convertContext) {
    if (StringUtil.isEmpty(s)) {
      return null;
    }

    return ContainerUtil.find(getStrutsPackages(convertContext), new Condition<StrutsPackage>() {
      public boolean value(final StrutsPackage strutsPackage) {
        return Comparing.equal(strutsPackage.getName().getStringValue(), s);
      }
    });
  }

  @Override
  public String toString(@Nullable final StrutsPackage strutsPackage, final ConvertContext convertContext) {
    return strutsPackage != null ? strutsPackage.getName().getStringValue() : null;
  }

  @Override
  public String getErrorMessage(@Nullable final String s, final ConvertContext convertContext) {
    return "Cannot resolve Struts package ''" + s + "''";
  }

  private static List<StrutsPackage> getStrutsPackages(final ConvertContext convertContext) {
    final XmlFile xmlFile = convertContext.getFile();
    final StrutsManager strutsManager = StrutsManager.getInstance(xmlFile.getProject());

    // find best matching StrutsModel
    final StrutsModel model;
    if (strutsManager.isStruts2ConfigFile(xmlFile)) {
      model = strutsManager.getModelByFile(xmlFile);
    } else {
      model = strutsManager.getCombinedModel(convertContext.getModule());
    }
    if (model == null) {
      return Collections.emptyList();
    }

    return model.getStrutsPackages();
  }

}