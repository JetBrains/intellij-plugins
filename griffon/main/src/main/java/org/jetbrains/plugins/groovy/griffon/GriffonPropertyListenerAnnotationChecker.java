// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.griffon;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.annotator.checkers.CustomAnnotationChecker;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.annotation.GrAnnotation;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.annotation.GrAnnotationMemberValue;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.annotation.GrAnnotationNameValuePair;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

/**
 * @author Max Medvedev
 */
public class GriffonPropertyListenerAnnotationChecker extends CustomAnnotationChecker {
  @Override
  public boolean checkArgumentList(@NotNull AnnotationHolder holder, @NotNull GrAnnotation annotation) {
    if (!"griffon.transform.PropertyListener".equals(annotation.getQualifiedName())) return false;

    final GrAnnotationNameValuePair[] attributes = annotation.getParameterList().getAttributes();
    if (attributes.length != 1) return false;

    final GrAnnotationNameValuePair attribute = attributes[0];
    final GrAnnotationMemberValue value = attribute.getValue();

    final PsiAnnotationOwner owner = annotation.getOwner();
    if (owner instanceof GrField) {
      if (value instanceof GrClosableBlock) {
        return true;
      }
    }
    else if (owner instanceof GrTypeDefinition) {
      if (value instanceof GrReferenceExpression) {
        final PsiElement resolved = ((GrReferenceExpression)value).resolve();
        if (resolved instanceof GrField) {
          final PsiClass containingClass = ((GrField)resolved).getContainingClass();
          if (annotation.getManager().areElementsEquivalent((PsiElement)owner, containingClass)) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
