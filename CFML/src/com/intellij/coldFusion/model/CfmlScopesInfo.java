// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model;

import com.intellij.coldFusion.model.psi.impl.CfmlFunctionParameterImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagFunctionParameterImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public final class CfmlScopesInfo {
  private static final String[] ourTypeNames = {
    "application",
    "arguments",
    "attributes",
    "caller",
    "cgi",
    "client",
    "cookie",
    "flash",
    "form",
    "request",
    "server",
    "session",
    // "this",
    "thistag",
    "thread",
    "url",
    "variables",
    "default",
    "super"
  };

  public static final int APPLICATION_SCOPE = 0;
  public static final int ARGUMENTS_SCOPE = 1;
  public static final int ATTRIBUTES_SCOPE = 2;
  public static final int CALLER_SCOPE = 3;
  public static final int CGI_SCOPE = 4;
  public static final int CLIENT_SCOPE = 5;
  public static final int COOKIE_SCOPE = 6;
  public static final int FLASH_SCOPE = 7;
  public static final int FORM_SCOPE = 8;
  public static final int REQUEST_SCOPE = 9;
  public static final int SERVER_SCOPE = 10;
  public static final int SESSION_SCOPE = 11;
  public static final int THIS_SCOPE = 12;
  public static final int THISTAG_SCOPE = 13;
  public static final int THREAD_SCOPE = 14;
  public static final int URL_SCOPE = 15;
  public static final int VARIABLES_SCOPE = 16;
  public static final int DEFAULT_SCOPE = 17;

  public static int getScopeByString(@Nullable String s) {
    if (s == null) {
      return DEFAULT_SCOPE;
    }
    final int find = ArrayUtil.find(ourTypeNames, StringUtil.toLowerCase(s));
    if (find != -1) {
      return find;
    }
    return DEFAULT_SCOPE;
  }

  public static boolean isConvenient(PsiElement targetElement, int scopeSource) {
    if (scopeSource == ARGUMENTS_SCOPE) {
      if (targetElement instanceof CfmlTagFunctionParameterImpl || targetElement instanceof CfmlFunctionParameterImpl) {
        return true;
      }
      return false;
    }
    return true;
  }
}
