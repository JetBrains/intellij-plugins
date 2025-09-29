// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Stores mappings for [parameter types](https://cucumber.io/docs/cucumber/configuration/#parameter-types) which are
/// present in [Cucumber Expressions](https://github.com/cucumber/cucumber-expressions), for example:
///
/// ## Terminology
///
/// In our Cucumber plugin, a single parameter type can have a value or a declaration, but not both.
///
/// The examples below are in Java, but the principle is the same for our all language-specific Cucumber plugins.
///
/// ### Parameter types with value
///
/// Parameter types that have value are common built-in parameter types like `int`, `float`, and `word`,
/// and Java built-in parameter types like `long` and `biginteger`.
///
/// Use [#getParameterTypeValue] to access them.
///
/// ### Parameter types with declaration
///
/// Parameter types that have declaration (also called "custom parameter types") are backed by some PSI element.
/// They can be defined by the developer.
///
/// Use [#getParameterTypeDeclaration] to access them.
///
/// A custom parameter type can be defined by annotating a method with Cucumber's `@ParameterType` annotation:
///
/// ```java
/// @ParameterType("\\d{4}-\\d{2}-\\d{2}")
/// public LocalDate isoDate(String dateStr) {
///   return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
/// }
/// ```
///
/// We can then use the parameter type in a step definition:
///
/// ```java
/// @And("yesterday was {isoDate}, and in {int} days it will be {isoDate}")
/// public void step_method_2(LocalDate yesterdayDate, int days, LocalDate futureDate) {
///     LocalDate actualFutureDate = yesterdayDate.plusDays(days + 1);
///     Assert.assertEquals(futureDate, actualFutureDate);
/// }
/// ```
///
/// And finally, we can use it in a feature file:
///
/// ```gherkin
/// Feature: Calendar
///
///   Scenario: Date calculation works correctly
///     Then yesterday was 2002-04-25, and in 3 days it will be 2002-04-29
/// ```
@NotNullByDefault
public interface ParameterTypeManager {
  /// @return value of the parameter type `name`
  @Nullable String getParameterTypeValue(String name);


  /// @return element (usually a string literal) that declares the parameter type `name`
  @Nullable PsiElement getParameterTypeDeclaration(String name);
}
