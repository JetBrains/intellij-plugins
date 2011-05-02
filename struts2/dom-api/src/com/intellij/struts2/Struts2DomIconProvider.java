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

package com.intellij.struts2;

import com.intellij.ide.presentation.PresentationIconProvider;
import com.intellij.struts2.dom.params.Param;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.ExceptionMapping;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.struts2.dom.validator.Validators;

import javax.swing.*;

/**
 * Provides icons for DOM elements.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2DomIconProvider implements PresentationIconProvider {

  @Override
  public Icon getIcon(final Object domElement, final int flags) {
    if (domElement instanceof Param) {
      return StrutsIcons.PARAM;
    }

    if (domElement instanceof Action) {
      return StrutsIcons.ACTION;
    }

    if (domElement instanceof Result) {
      return StrutsIcons.RESULT;
    }

    if (domElement instanceof StrutsPackage) {
      return StrutsIcons.PACKAGE;
    }

    if (domElement instanceof DefaultInterceptorRef) {
      return StrutsIcons.DEFAULT_INTERCEPTOR_REF;
    }

    if (domElement instanceof DefaultActionRef) {
      return StrutsIcons.DEFAULT_ACTION_REF;
    }

    if (domElement instanceof DefaultClassRef) {
      return StrutsIcons.DEFAULT_CLASS_REF;
    }

    if (domElement instanceof GlobalResult) {
      return StrutsIcons.GLOBAL_RESULT;
    }

    if (domElement instanceof GlobalExceptionMapping) {
      return StrutsIcons.GLOBAL_EXCEPTION_MAPPING;
    }

    if (domElement instanceof ExceptionMapping) {
      return StrutsIcons.EXCEPTION_MAPPING;
    }

    if (domElement instanceof StrutsRoot) {
      return StrutsIcons.STRUTS_CONFIG_FILE_ICON;
    }

    if (domElement instanceof Interceptor) {
      return StrutsIcons.INTERCEPTOR;
    }

    if (domElement instanceof InterceptorStack) {
      return StrutsIcons.INTERCEPTOR_STACK;
    }

    if (domElement instanceof InterceptorRef) {
      final InterceptorOrStackBase interceptorOrStackBase = ((InterceptorRef) domElement).getName().getValue();
      if (interceptorOrStackBase instanceof Interceptor) {
        return StrutsIcons.INTERCEPTOR;
      }
      if (interceptorOrStackBase instanceof InterceptorStack) {
        return StrutsIcons.INTERCEPTOR_STACK;
      }
    }

    // validator
    if (domElement instanceof Validators) {
      return StrutsIcons.VALIDATION_CONFIG_FILE_ICON;
    }

    return null;
  }

}