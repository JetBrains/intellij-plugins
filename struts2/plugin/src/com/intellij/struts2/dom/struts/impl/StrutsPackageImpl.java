/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.struts2.dom.struts.strutspackage.DefaultClassRef;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.structure.LocationPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public abstract class StrutsPackageImpl implements StrutsPackage, LocationPresentation {

  public String getLocation() {
    return getNamespace().getStringValue();
  }

  @NotNull
  public String searchNamespace() {
    final String namespace = getNamespace().getStringValue();
    return namespace != null ? namespace : DEFAULT_NAMESPACE;
  }

  @Nullable
  public DefaultClassRef searchDefaultClassRef() {
    StrutsPackage currentPackage = this;
    while (currentPackage != null) {
      if (currentPackage.getDefaultClassRef().getXmlElement() != null) {
        return currentPackage.getDefaultClassRef();
      }
      currentPackage = currentPackage.getExtends().getValue();
    }
    return null;
  }

  @Nullable
  public ResultType searchDefaultResultType() {
    StrutsPackage currentPackage = this;
    while (currentPackage != null) {
      final List<ResultType> resultTypes = currentPackage.getResultTypes();
      for (final ResultType resultType : resultTypes) {
        if (resultType.getDefault().getXmlElement() != null) {
          return resultType;
        }
      }
      currentPackage = currentPackage.getExtends().getValue();
    }

    return null;
  }

}