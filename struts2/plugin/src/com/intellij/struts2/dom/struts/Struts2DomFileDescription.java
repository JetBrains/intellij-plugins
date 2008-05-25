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

package com.intellij.struts2.dom.struts;

import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.Param;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.struts2.dom.impl.ParamImpl;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.impl.*;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.util.xml.DomFileDescription;

/**
 * <code>struts.xml</code> DOM-Model files.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2DomFileDescription extends DomFileDescription<StrutsRoot> {

  public Struts2DomFileDescription() {
    super(StrutsRoot.class, StrutsRoot.TAG_NAME);
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(StrutsDomConstants.STRUTS_NAMESPACE_KEY,
                            StrutsConstants.STRUTS_2_0_DTD_URI,
                            StrutsConstants.STRUTS_2_0_DTD_ID);

    registerImplementation(Action.class, ActionImpl.class);
    registerImplementation(Constant.class, ConstantImpl.class);
    registerImplementation(DefaultActionRef.class, DefaultActionRefImpl.class);
    registerImplementation(DefaultClassRef.class, DefaultClassRefImpl.class);
    registerImplementation(DefaultInterceptorRef.class, DefaultInterceptorRefImpl.class);
    registerImplementation(GlobalResult.class, GlobalResultImpl.class);
    registerImplementation(InterceptorRef.class, InterceptorRefImpl.class);
    registerImplementation(Param.class, ParamImpl.class);
    registerImplementation(Result.class, ResultImpl.class);
    registerImplementation(StrutsPackage.class, StrutsPackageImpl.class);
  }


}