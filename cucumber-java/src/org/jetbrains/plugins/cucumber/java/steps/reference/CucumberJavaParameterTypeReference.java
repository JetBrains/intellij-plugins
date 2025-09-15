// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.reference;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.pom.references.PomService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.MapParameterTypeManager;
import org.jetbrains.plugins.cucumber.ParameterTypeManager;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

//@formatter:off Temporarily disable formatter because of bug IDEA-371809
/// A reference from a [parameter type](https://cucumber.io/docs/cucumber/configuration#parameter-types) usage inside
/// a definition of a [Cucumber Expression](https://github.com/cucumber/cucumber-expressions)
/// to the parameter type's definition.
///
/// ### Example
///
/// For example, we provide a reference from the `"iso-date"` parameter in the step definition:
///
/// ```
/// @Given("today is {isoDate}")
/// public void todayIs(Date date) throws Throwable {
///   // ...
/// }
/// ```
/// to the parameter type definition.
///
/// A parameter type (the thing this reference points to) can be defined in 2 ways:
///
/// First, using Cucumber's old, deprecated `TypeRegistry` API:
///
/// ```
/// typeRegistry.defineParameterType(new ParameterType<>(
///   "isoDate",
///   "\\d{4}-\\d{2}-\\d{2}",
///   Date.class,
///   (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
/// ));
/// ```
///
/// Second, using Cucumber's new `@ParameterType` annotation:
///
/// ```
/// @ParameterType("\\d{4}-\\d{2}-\\d{2}")
/// public Date isoDate(String input) {
///   return new SimpleDateFormat("yyyy-MM-dd").parse(input);
/// }
/// ```
/// @see CucumberJavaUtil#processParameterTypesDefinedByAnnotation
/// @see CucumberJavaUtil#processParameterTypesDefinedByTypeRegistry
//@formatter:on
@NotNullByDefault
public class CucumberJavaParameterTypeReference extends PsiReferenceBase<PsiElement> {
  public CucumberJavaParameterTypeReference(PsiElement element, TextRange range) {
    // Exclude { and }
    super(element, TextRange.create(range.getStartOffset() + 1, range.getEndOffset() - 1), false);
  }


  @Override
  public @Nullable PsiElement resolve() {
    String parameterTypeName = getParameterTypeName();
    final Module module = ModuleUtilCore.findModuleForPsiElement(myElement);
    if (module != null) {
      ParameterTypeManager manager = CucumberJavaUtil.getAllParameterTypes(module);

      PsiElement declaration = manager.getParameterTypeDeclaration(parameterTypeName);
      if (declaration != null) {
        return PomService.convertToPsi(new CucumberJavaParameterPomTarget(declaration, parameterTypeName));
      }
    }
    return null;
  }

  @Override
  public String getCanonicalText() {
    return getParameterTypeName();
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    if (!(element instanceof PsiNamedElement namedElement) || !(element instanceof PomTargetPsiElement psiElement)) {
      return false;
    }
    PomTarget pomTarget = psiElement.getTarget();
    if (!(pomTarget instanceof CucumberJavaParameterPomTarget)) {
      return false;
    }
    String parameterTypeName = getParameterTypeName();
    if (!StringUtil.equals(namedElement.getName(), parameterTypeName)) {
      return false;
    }
    PsiElement resolved = resolve();
    return resolved != null && resolved.equals(element);
  }

  private String getParameterTypeName() {
    return getRangeInElement().substring(myElement.getText());
  }
}
