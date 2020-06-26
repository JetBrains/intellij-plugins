package com.intellij.tapestry.intellij.util;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public final class TapestryPropertyNamingUtil {
  private TapestryPropertyNamingUtil() {
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static boolean isWaitOrNotifyOfObject(@NotNull PsiMethod method) {
    PsiClass psiClass = method.getContainingClass();
    if (psiClass == null || !CommonClassNames.JAVA_LANG_OBJECT.equals(psiClass.getQualifiedName())) {
      return false;
    }
    String name = method.getName();
    return "wait".equals(name) || "notify".equals(name) || "notifyAll".equals(name);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static boolean isPropertyGetter(@NotNull PsiMethod method) {
    if (method.isConstructor()) {
      return false;
    }
    String methodName = method.getName();
    PsiType returnType = method.getReturnType();
    if (methodName.startsWith("get") && methodName.length() > "get".length()) {
      if (returnType == null || PsiType.VOID.equals(returnType)) return false;
    }
    else if (methodName.startsWith("is")) {
      if (!PsiType.BOOLEAN.equals(returnType)) return false;
    }
    else {
      return false;
    }
    return method.getParameterList().getParametersCount() == 0;
  }

  public static boolean isPropertySetter(@NotNull PsiMethod method) {
    if (method.isConstructor()) {
      return false;
    }
    String methodName = method.getName();
    return methodName.startsWith("set") &&
           methodName.length() > "set".length() &&
           method.getParameterList().getParametersCount() == 1 &&
           (method.getReturnType() == null || PsiType.VOID.equals(method.getReturnType()));
  }

  public static boolean isPropertyAccessor(PsiMethod method) {
    return isPropertyGetter(method) || isPropertySetter(method);
  }

  @Nullable
  public static String getPropertyNameFromAccessor(@NotNull PsiMethod accessor) {
    if (isPropertySetter(accessor)) {
      return getPropertyNameFromSetter(accessor);
    }
    if (isPropertyGetter(accessor)) {
      return getPropertyNameFromGetter(accessor);
    }
    return null;
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private static String getPropertyNameFromGetter(@NotNull PsiMethod getter) {
    String methodName = getter.getName();
    int prefixLength = methodName.startsWith("get") ? "get".length() : "is".length();
    return methodName.substring(prefixLength);
  }

  private static String getPropertyNameFromSetter(@NotNull PsiMethod setter) {
    return setter.getName().substring("set".length());
  }


  @Nullable
  public static PsiMethod findPropertyGetter(final PsiClass aClass, final String propertyName) {
    return findPropertyAccessor(aClass, propertyName, new PropertyNameExtractor() {
      @Override
      public String extractPropertyName(@NotNull PsiMethod method) {
        return isPropertyGetter(method) ? getPropertyNameFromGetter(method) : null;
      }
    });
  }

  @Nullable
  public static PsiMethod findPropertySetter(final PsiClass aClass, final String propertyName) {
    return findPropertyAccessor(aClass, propertyName, new PropertyNameExtractor() {
      @Override
      public String extractPropertyName(@NotNull PsiMethod method) {
        return isPropertySetter(method) ? getPropertyNameFromSetter(method) : null;
      }
    });
  }

  @Nullable
  private static PsiMethod findPropertyAccessor(final PsiClass aClass, final String propertyName, PropertyNameExtractor extractor) {
    if (aClass == null) {
      return null;
    }
    for (PsiMethod method : aClass.getAllMethods()) {
      String wouldBePropertyName = extractor.extractPropertyName(method);
      if (propertyName.equalsIgnoreCase(wouldBePropertyName)) {
        return method;
      }
    }
    return null;
  }

  private interface PropertyNameExtractor {
    @Nullable
    String extractPropertyName(@NotNull PsiMethod method);
  }
}