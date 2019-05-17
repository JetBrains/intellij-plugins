// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.psi.PsiElement;
import org.angular2.codeInsight.attributes.Angular2EventHandlerDescriptor;

import java.util.Collections;

/**
 * @deprecated Kept for compatibility with NativeScript
 * To be removed in 2019.2
 */
@Deprecated
public class AngularEventHandlerDescriptor extends Angular2EventHandlerDescriptor {
  public AngularEventHandlerDescriptor(PsiElement element, String attributeName) {
    super(null, attributeName, AttributePriority.NONE, Collections.singletonList(element), true);
  }
}
