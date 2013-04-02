package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.AnalyzerMessage;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DartTypeErrorCode {
  CANNOT_BE_RESOLVED {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // cannot resolve %s
      return findFixesForUnresolved(file, startOffset);
    }
  },
  NOT_A_MEMBER_OF {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // "%s" is not a member of %s
      return findFixesForUnresolved(file, startOffset);
    }
  }, CONCRETE_CLASS_WITH_UNIMPLEMENTED_MEMBERS {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // Concrete class %s has unimplemented member(s) %s
      return Arrays.asList(new ImplementMethodAction(startOffset));
    }
  }, EXTRA_ARGUMENT {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // todo: extra argument
      return Collections.emptyList();
    }
  }, FIELD_HAS_NO_GETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // Field '%s' has no getter
      PsiElement elementAt = file.findElementAt(startOffset);
      return elementAt == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartGetterSetterAction(
        elementAt.getText(),
        true,
        false
      ));
    }
  }, FIELD_HAS_NO_SETTER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // Field '%s' has no setter
      PsiElement elementAt = file.findElementAt(startOffset);
      return elementAt == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartGetterSetterAction(
        elementAt.getText(),
        false,
        false
      ));
    }
  }, INTERFACE_HAS_NO_METHOD_NAMED {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // "%s" has no method named "%s"
      final String messageText = message.getMessage();
      String functionName = DartPresentableUtil.findLastDoubleQuotedWord(messageText);
      if (functionName == null) {
        return Collections.emptyList();
      }
      return Arrays.asList(
        functionName.startsWith("operator ") ? new CreateDartOperatorAction(functionName)
                                             : new CreateDartMethodAction(functionName, false)
      );
    }
  }, NO_SUCH_NAMED_PARAMETER {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // todo:  no such named parameter %s defined
      return Collections.emptyList();
    }
  }, NO_SUCH_TYPE {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // no such type "%s"
      String className = DartPresentableUtil.findLastDoubleQuotedWord(message.getMessage());
      return className == null ? Collections.<IntentionAction>emptyList() : Arrays.asList(new CreateDartClassAction(className));
    }
  }, PLUS_CANNOT_BE_USED_FOR_STRING_CONCAT {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // todo:  '%s' cannot be used for string concatentation, use string interpolation or a StringBuffer instead
      return Collections.emptyList();
    }
  }, STATIC_MEMBER_ACCESSED_THROUGH_INSTANCE {
    @NotNull
    @Override
    public List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message) {
      // todo:  static member %s of %s cannot be accessed through an instance
      return Collections.emptyList();
    }
  };

  private static List<? extends IntentionAction> findFixesForUnresolved(PsiFile file, int startOffset) {
    final DartReference reference = PsiTreeUtil.getParentOfType(file.findElementAt(startOffset), DartReference.class);
    final DartClass dartClass = PsiTreeUtil.getParentOfType(reference, DartClass.class);
    final String name = reference != null ? reference.getText() : null;
    if (reference == null || name == null) {
      return Collections.emptyList();
    }
    final boolean isLValue = DartResolveUtil.isLValue(reference);
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);

    final ArrayList<IntentionAction> result = new ArrayList<IntentionAction>();

    // chain
    if (leftReference != null) {
      final PsiElement leftTarget = leftReference.resolve();
      final DartComponentType leftTargetType = DartComponentType.typeOf(leftTarget != null ? leftTarget.getParent() : null);
      result.add(new CreateDartGetterSetterAction(name, !isLValue, leftTargetType == DartComponentType.CLASS));
      result.add(new CreateFieldAction(name, leftTargetType == DartComponentType.CLASS));
      if (DartResolveUtil.aloneOrFirstInChain(reference)) {
        result.add(new CreateGlobalDartGetterSetterAction(name, false));
      }
    }

    // alone with capital. seems class name.
    if (DartResolveUtil.aloneOrFirstInChain(reference) && StringUtil.isCapitalized(name)) {
      result.add(new CreateDartClassAction(name));
    }

    // alone. not class.
    if (DartResolveUtil.aloneOrFirstInChain(reference) && !StringUtil.isCapitalized(name)) {
      final DartComponent parentComponent = PsiTreeUtil.getParentOfType(reference, DartComponent.class);
      if (reference.getParent() instanceof DartCallExpression) {
        result.add(new CreateGlobalDartFunctionAction(name));
        if (dartClass != null) {
          result.add(new CreateDartMethodAction(name, parentComponent != null && parentComponent.isStatic()));
        }
      }
      else {
        result.add(new CreateDartGetterSetterAction(name, !isLValue, parentComponent != null && parentComponent.isStatic()));
        result.add(new CreateGlobalDartGetterSetterAction(name, !isLValue));
        result.add(new CreateFieldAction(name, parentComponent != null && parentComponent.isStatic()));
        result.add(new CreateGlobalVariableAction(name));
        result.add(new CreateLocalVariableAction(name));
      }
    }

    return result;
  }

  @NotNull
  public abstract List<? extends IntentionAction> getFixes(@NotNull PsiFile file, int startOffset, @NotNull AnalyzerMessage message);

  public static DartTypeErrorCode findError(String code) {
    try {
      return valueOf(code);
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }
}
