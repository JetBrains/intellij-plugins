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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.jam.model.common.BaseImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.struts2.dom.struts.strutspackage.DefaultClassRef;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackageHierarchyWalker;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public abstract class StrutsPackageImpl extends BaseImpl implements StrutsPackage {

  @Override
  @NotNull
  public String searchNamespace() {
    final Ref<String> result = new Ref<>();
    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(this, strutsPackage -> {
      if (DomUtil.hasXml(strutsPackage.getNamespace())) {
        result.set(strutsPackage.getNamespace().getStringValue());
        return false;
      }
      return true;
    });
    walker.walkUp();

    return result.isNull() ? DEFAULT_NAMESPACE : result.get();
  }

  @Override
  @Nullable
  public DefaultClassRef searchDefaultClassRef() {
    final Ref<DefaultClassRef> result = new Ref<>();
    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(this, strutsPackage -> {
      if (DomUtil.hasXml(strutsPackage.getDefaultClassRef())) {
        result.set(strutsPackage.getDefaultClassRef());
        return false;
      }
      return true;
    });
    walker.walkUp();

    return result.get();
  }

  private CachedValue<ResultType> myCachedDefaultResultType;

  @Override
  @Nullable
  public ResultType searchDefaultResultType() {
    if (myCachedDefaultResultType == null) {
      final PsiFile containingFile = getContainingFile();
      if (containingFile == null) {
        return null;
      }

      myCachedDefaultResultType = CachedValuesManager.getManager(containingFile.getProject()).createCachedValue(
        () -> {
          final Ref<ResultType> result = new Ref<>();
          final StrutsPackageHierarchyWalker walker =
            new StrutsPackageHierarchyWalker(this, strutsPackage -> {
              final List<ResultType> resultTypes = strutsPackage.getResultTypes();
              for (final ResultType resultType : resultTypes) {
                final GenericAttributeValue<Boolean> defaultAttribute = resultType.getDefault();
                if (DomUtil.hasXml(defaultAttribute) &&
                    defaultAttribute.getValue() == Boolean.TRUE) {
                  result.set(resultType);
                  return false;
                }
              }
              return true;
            });
          walker.walkUp();

          return CachedValueProvider.Result.createSingleDependency(result.get(), PsiModificationTracker.MODIFICATION_COUNT);
        }, false);
    }

    return myCachedDefaultResultType.getValue();
  }
}