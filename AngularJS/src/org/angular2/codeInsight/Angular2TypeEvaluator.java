// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.lang.javascript.psi.resolve.context.JSApplyCallElement;
import com.intellij.lang.javascript.psi.resolve.context.JSApplyContextElement;
import com.intellij.util.ArrayUtil;
import org.angular2.lang.expr.psi.Angular2PipeExpression;

public class Angular2TypeEvaluator extends TypeScriptTypeEvaluator {

  protected Angular2TypeEvaluator(JSEvaluateContext context,
                                  JSTypeProcessor processor) {
    super(context, processor);
  }

  @Override
  protected boolean evaluateDialectSpecificTypes(JSExpression rawQualifier) {
    if (rawQualifier instanceof Angular2PipeExpression) {
      Angular2PipeExpression pipeExpression = (Angular2PipeExpression)rawQualifier;
      JSExpression pipeName = pipeExpression.getNameReference();
      final JSApplyContextElement elementToApply = new JSApplyCallElement(
        pipeName, ArrayUtil.prepend(pipeExpression.getExpression(), pipeExpression.getArguments()));
      myContext.processWithJSElementToApply(elementToApply, () -> evaluateTypes(pipeName, JSEvaluateContext.JSEvaluationPlace.DEFAULT));
      return true;
    }
    return super.evaluateDialectSpecificTypes(rawQualifier);
  }
}
