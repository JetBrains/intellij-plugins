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

import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.struts.strutspackage.Interceptor;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRef;
import org.jetbrains.annotations.Nullable;

/**
 * @author Yann C&eacute;bron
 */
public abstract class InterceptorRefImpl implements InterceptorRef {

  @Override
  @Nullable
  public PsiClass getParamsClass() {
    final InterceptorOrStackBase interceptorOrStack = getName().getValue();
    if (interceptorOrStack instanceof Interceptor) {
      return ((Interceptor) interceptorOrStack).getInterceptorClass().getValue();
    }
    return null;
  }

}