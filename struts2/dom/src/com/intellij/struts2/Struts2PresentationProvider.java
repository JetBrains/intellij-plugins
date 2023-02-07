/*
 * Copyright 2019 The authors
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

import com.intellij.icons.AllIcons;
import com.intellij.ide.presentation.PresentationProvider;
import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.struts.Include;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.ExceptionMapping;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;

import javax.swing.*;

/**
 * Provides icon/name for Struts DOM elements.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2PresentationProvider extends PresentationProvider {

  @Override
  public Icon getIcon(final Object o) {

    if (o instanceof DefaultInterceptorRef) {
      return StrutsIcons.DEFAULT_INTERCEPTOR_REF;
    }

    if (o instanceof DefaultActionRef) {
      return StrutsIcons.DEFAULT_ACTION_REF;
    }

    if (o instanceof DefaultClassRef) {
      return StrutsIcons.DEFAULT_CLASS_REF;
    }

    if (o instanceof GlobalResult) {
      return StrutsIcons.GLOBAL_RESULT;
    }

    if (o instanceof GlobalExceptionMapping) {
      return StrutsIcons.GLOBAL_EXCEPTION_MAPPING;
    }

    if (o instanceof ExceptionMapping) {
      return AllIcons.Nodes.ExceptionClass;
    }

    if (o instanceof StrutsRoot) {
      return StrutsIcons.STRUTS_CONFIG_FILE;
    }

    if (o instanceof StrutsPackage) {
      return StrutsIcons.STRUTS_PACKAGE;
    }

    if (o instanceof InterceptorRef) {
      final InterceptorOrStackBase interceptorOrStackBase = ((InterceptorRef) o).getName().getValue();
      if (interceptorOrStackBase instanceof Interceptor) {
        return AllIcons.Nodes.Plugin;
      }
      if (interceptorOrStackBase instanceof InterceptorStack) {
        return AllIcons.Nodes.Pluginobsolete;
      }
    }

    if (o instanceof ResultType resultType) {
      final GenericAttributeValue<Boolean> resultTypeDefault = resultType.getDefault();
      if (DomUtil.hasXml(resultTypeDefault) &&
          resultTypeDefault.getValue() == Boolean.TRUE) {
        return StrutsIcons.RESULT_TYPE_DEFAULT;
      }
      return AllIcons.Debugger.Console;
    }

    return null;
  }

  @Override
  public String getName(final Object o) {

    if (o instanceof Result) {
      return ((Result) o).getNameOrDefault();
    }

    if (o instanceof StrutsRoot) {
      return DomUtil.getFile(((StrutsRoot) o)).getName();
    }

    if (o instanceof ExceptionMapping) {
      final PsiClass exceptionClass = ((ExceptionMapping) o).getExceptionClass().getValue();
      if (exceptionClass != null) {
        return exceptionClass.getName();
      }
      return ((ExceptionMapping) o).getName().getStringValue();
    }

    if (o instanceof GlobalExceptionMapping) {
      final PsiClass exceptionClass = ((GlobalExceptionMapping) o).getExceptionClass().getValue();
      if (exceptionClass != null) {
        return exceptionClass.getName();
      }
      return ((GlobalExceptionMapping) o).getName().getStringValue();
    }

    if (o instanceof InterceptorRef) {
      return ((InterceptorRef) o).getName().getStringValue();
    }

    if (o instanceof DefaultInterceptorRef) {
      return ((DefaultInterceptorRef) o).getName().getStringValue();
    }

    if (o instanceof Include) {
      return ((Include) o).getFile().getStringValue();
    }

    if (o instanceof GlobalResult) {
      final String globalResultName = ((GlobalResult) o).getName().getStringValue();
      return globalResultName != null ? globalResultName : Result.DEFAULT_NAME;
    }

    if (o instanceof DefaultActionRef) {
      return ((DefaultActionRef) o).getName().getStringValue();
    }

    if (o instanceof DefaultClassRef) {
      return ((DefaultClassRef) o).getDefaultClass().getStringValue();
    }

    return null;
  }

}
