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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.javaee.model.xml.impl.BaseImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.struts2.dom.struts.strutspackage.DefaultClassRef;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackageHierarchyWalker;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class StrutsPackageImpl extends BaseImpl implements StrutsPackage {

  @NotNull
  public String searchNamespace() {
    final Ref<String> result = new Ref<String>();
    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(this, new Processor<StrutsPackage>() {
      @Override
      public boolean process(final StrutsPackage strutsPackage) {
        if (DomUtil.hasXml(strutsPackage.getNamespace())) {
          result.set(strutsPackage.getNamespace().getStringValue());
          return true;
        }
        return false;
      }
    });
    walker.walkUp();

    return result.isNull() ? DEFAULT_NAMESPACE : result.get();
  }

  @Nullable
  public DefaultClassRef searchDefaultClassRef() {
    final Ref<DefaultClassRef> result = new Ref<DefaultClassRef>();
    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(this, new Processor<StrutsPackage>() {
      @Override
      public boolean process(final StrutsPackage strutsPackage) {
        if (DomUtil.hasXml(strutsPackage.getDefaultClassRef())) {
          result.set(strutsPackage.getDefaultClassRef());
          return true;
        }
        return false;
      }
    });
    walker.walkUp();

    return result.get();
  }

  @Nullable
  public ResultType searchDefaultResultType() {
    final Ref<ResultType> result = new Ref<ResultType>();
    final StrutsPackageHierarchyWalker walker = new StrutsPackageHierarchyWalker(this, new Processor<StrutsPackage>() {
      @Override
      public boolean process(final StrutsPackage strutsPackage) {
        final List<ResultType> resultTypes = strutsPackage.getResultTypes();
        for (final ResultType resultType : resultTypes) {
          if (resultType.getDefault().getValue() == Boolean.TRUE) {
            result.set(resultType);
            return true;
          }
        }
        return false;
      }
    });
    walker.walkUp();

    return result.get();
  }

}