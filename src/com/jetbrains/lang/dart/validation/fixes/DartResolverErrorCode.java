package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.analyzer.AnalyzerMessage;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.lang.dart.validation.fixes.DartFixesUtil.findFixesForUnresolved;
import static com.jetbrains.lang.dart.validation.fixes.DartFixesUtil.isStaticContext;

public enum DartResolverErrorCode {
  UNDEFINED_IDENTIFIER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      return findFixesForUnresolved(file, startOffset);
    }
  }, UNDEFINED_CLASS {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // Undefined class '%s'
      String className = DartPresentableUtil.findLastQuotedWord(message.getMessage());
      return className == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartClassAction(className));
    }
  },
  UNDEFINED_GETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      String name = DartPresentableUtil.findFirstQuotedWord(message.getMessage());
      return name == null ?
             Collections.<IntentionAction>emptyList() :
             Arrays.asList(new CreateDartGetterSetterAction(name, true, isStaticContext(file, startOffset)));
    }
  },
  UNDEFINED_SETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      String name = DartPresentableUtil.findFirstQuotedWord(message.getMessage());
      return name == null ?
             Collections.<IntentionAction>emptyList() :
             Arrays.asList(new CreateDartGetterSetterAction(name, false, isStaticContext(file, startOffset)));
    }
  }, UNDEFINED_OPERATOR {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // There is no such operator '' in 'A'
      String operator = DartPresentableUtil.findFirstQuotedWord(message.getMessage());
      return operator == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartOperatorAction(operator));
    }
  },
  CANNOT_RESOLVE_METHOD {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // cannot resolve method '%s'
      final String messageText = message.getMessage();
      String functionName = DartPresentableUtil.findLastQuotedWord(messageText);
      return functionName == null ? Collections.<IntentionAction>emptyList()
                                  : Arrays.asList(new CreateGlobalDartFunctionAction(functionName));
    }
  }, CANNOT_RESOLVE_METHOD_IN_CLASS {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // cannot resolve method '%s' in class '%s'
      final String messageText = message.getMessage();
      String functionName = DartPresentableUtil.findFirstQuotedWord(messageText);
      return functionName == null ? Collections.<IntentionAction>emptyList()
                                  : Arrays.asList(new CreateDartMethodAction(functionName, true));
    }
  }, FIELD_DOES_NOT_HAVE_A_GETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // Field does not have a getter
      PsiElement elementAt = file.findElementAt(startOffset);
      return elementAt == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartGetterSetterAction(
        elementAt.getText(),
        true,
        true
      ));
    }
  }, FIELD_DOES_NOT_HAVE_A_SETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // Field does not have a setter
      PsiElement elementAt = file.findElementAt(startOffset);
      return elementAt == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartGetterSetterAction(
        elementAt.getText(),
        false,
        true
      ));
    }
  },
  NOT_A_STATIC_FIELD {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // "%s" is not a static field
      DartComponent target = DartResolveUtil.findReferenceAndComponentTarget(file.findElementAt(startOffset));
      return target == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new MakeStaticAction(target));
    }
  }, NOT_A_STATIC_METHOD {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // "%s" is not a static method
      DartComponent target = DartResolveUtil.findReferenceAndComponentTarget(file.findElementAt(startOffset));
      return target == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new MakeStaticAction(target));
    }
  };

  @NotNull
  public abstract List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message);

  public static DartResolverErrorCode findError(String code) {
    try {
      return valueOf(code);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }
}
