package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.ide.DartWritingAccessProvider;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.lang.dart.validation.fixes.DartFixesUtil.findFixesForUnresolved;
import static com.jetbrains.lang.dart.validation.fixes.DartFixesUtil.isStaticContext;

public enum DartWarningCode {
  UNDEFINED_METHOD {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // The method '%s' is not defined for the class '%s'
      String functionName = DartPresentableUtil.findFirstQuotedWord(message);
      if (functionName == null) {
        return Collections.emptyList();
      }
      return Arrays.asList(new CreateDartMethodAction(functionName, isStaticContext(file, startOffset)));
    }
  },
  UNDEFINED_FUNCTION {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // The function '%s' is not defined
      return findFixesForUnresolved(file, startOffset);
    }
  },
  UNDEFINED_IDENTIFIER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Undefined name '%s'
      return findFixesForUnresolved(file, startOffset);
    }
  },
  UNDEFINED_CLASS {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Undefined class '%s'
      String className = DartPresentableUtil.findLastQuotedWord(message);
      if (className != null) {
        final List<BaseCreateFix> result = new SmartList<BaseCreateFix>(new CreateDartClassAction(className));
        DartFixesUtil.suggestImports(result, file, className);
        return result;
      }
      return Collections.<IntentionAction>emptyList();
    }
  },
  UNDEFINED_GETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // There is no such getter '%s' in '%s'
      String name = DartPresentableUtil.findFirstQuotedWord(message);
      return name == null ?
             Collections.<IntentionAction>emptyList() :
             Arrays.asList(new CreateDartGetterSetterAction(name, true, isStaticContext(file, startOffset)));
    }
  },
  UNDEFINED_SETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // There is no such setter '%s' in '%s'
      String name = DartPresentableUtil.findFirstQuotedWord(message);
      return name == null ?
             Collections.<IntentionAction>emptyList() :
             Arrays.asList(new CreateDartGetterSetterAction(name, false, isStaticContext(file, startOffset)));
    }
  },
  UNDEFINED_OPERATOR {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // There is no such operator '%s' in '%s'
      String operator = DartPresentableUtil.findFirstQuotedWord(message);
      return operator == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartOperatorAction(operator));
    }
  },
  STATIC_ACCESS_TO_INSTANCE_MEMBER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Instance member '%s' cannot be accessed using static access
      DartComponent target = DartResolveUtil.findReferenceAndComponentTarget(file.findElementAt(startOffset));
      return target == null || DartWritingAccessProvider.isInDartSdkOrDartPackagesFolder(target.getContainingFile())
             ? Collections.<IntentionAction>emptyList()
             : Arrays.asList(new MakeStaticAction(target));
    }
  },
  NON_ABSTRACT_CLASS_INHERITS_ABSTRACT_MEMBER_FIVE_PLUS {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Missing inherited members: '%s', '%s', '%s', '%s' and %d more
      return Arrays.asList(new ImplementMethodAction(startOffset));
    }
  },
  NON_ABSTRACT_CLASS_INHERITS_ABSTRACT_MEMBER_FOUR {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Missing inherited members: '%s', '%s', '%s' and '%s'
      return Arrays.asList(new ImplementMethodAction(startOffset));
    }
  },
  NON_ABSTRACT_CLASS_INHERITS_ABSTRACT_MEMBER_THREE {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Missing inherited members: '%s', '%s' and '%s'
      return Arrays.asList(new ImplementMethodAction(startOffset));
    }
  },
  NON_ABSTRACT_CLASS_INHERITS_ABSTRACT_MEMBER_TWO {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Missing inherited members: '%s' and '%s'
      return Arrays.asList(new ImplementMethodAction(startOffset));
    }
  },
  NON_ABSTRACT_CLASS_INHERITS_ABSTRACT_MEMBER_ONE {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // Missing inherited member '%s'
      return Arrays.asList(new ImplementMethodAction(startOffset));
    }
  },
  CAST_TO_NON_TYPE {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message) {
      // The name '%s' is not a type and cannot be used in an 'as' expression
      String className = DartPresentableUtil.findFirstQuotedWord(message);
      if (className != null) {
        final List<BaseCreateFix> result = new SmartList<BaseCreateFix>(new CreateDartClassAction(className));
        DartFixesUtil.suggestImports(result, file, className);
        return result;
      }
      return Collections.<IntentionAction>emptyList();
    }
  };

  @NotNull
  public abstract List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull String message);

  public static DartWarningCode findError(String code) {
    try {
      return valueOf(code);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }
}
