// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.ArrayUtil;
import com.intellij.util.BooleanFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.BDDAssertions.then;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class CompletionTestCase extends BasePlatformTestCase {
  protected int myCompleteInvocationCount = 1;

  protected abstract String getFileName();

  protected abstract Language getExpectedLanguage();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCompleteInvocationCount = 1;
  }

  protected void doBasicCompletionTest(String text, Collection<String> expected) {
    doBasicCompletionTest(text, expected.size(), ArrayUtil.toStringArray(expected));
  }

  protected void doBasicCompletionTest(String text, final int expectedAllSize, final String... expected) {
    doBasicCompletionTest(text, getPartialMatcher(expectedAllSize, expected));
  }

  protected void doBasicCompletionTest(String text, final String... expected) {
    doBasicCompletionTest(text, getPartialMatcher(expected));
  }

  protected void doBasicCompletionTest(String text, Predicate<Collection<String>> matcher) {
    final PsiFile psiFile = myFixture.configureByText(getFileName(), text);
    assertEquals(getExpectedLanguage(), psiFile.getLanguage());
    final LookupElement[] elements = myFixture.complete(CompletionType.BASIC, myCompleteInvocationCount);
    assertNotNull(elements);
    final List<String> strings = myFixture.getLookupElementStrings();
    assertNotNull(strings);
    assertTrue("Matcher expected to return true", matcher.test(strings));
  }

  public static abstract class Matcher implements Predicate<Collection<String>> {
    public static Matcher and(final Matcher first, final Matcher second, final Matcher... rest){
      return new Matcher() {
        @Override
        public boolean test(Collection<String> strings) {
          if (!first.test(strings)) return false;
          if (!second.test(strings)) return false;
          for (Matcher matcher : rest) {
            if (!matcher.test(strings)) return false;
          }
          return true;
        }
      };
    }

    public static Matcher all(final String... unexpected) {
      return new Matcher() {
        @Override
        public boolean test(Collection<String> strings) {
          then(strings).contains(unexpected);
          return true;
        }
      };
    }

    public static Matcher not(final String... unexpected) {
      return new Matcher() {
        @Override
        public boolean test(Collection<String> strings) {
          then(strings).doesNotContain(unexpected);
          return true;
        }
      };
    }
  }

  @NotNull
  protected BooleanFunction<Collection<String>> getExactMatcher(final String... expectedPart) {
    return strings -> {
      then(strings).containsOnly(expectedPart);
      return true;
    };
  }

  @NotNull
  protected Predicate<Collection<String>> getPartialMatcher(final String... expectedPart) {
    return strings -> {
      then(strings).contains(expectedPart);
      return true;
    };
  }

  @NotNull
  protected Predicate<Collection<String>> getPartialMatcher(final Collection<String> expectedPart) {
    return strings -> {
      then(strings).containsAll(expectedPart);
      return true;
    };
  }

  @NotNull
  protected Predicate<Collection<String>> getPartialMatcher(final int expectedAllSize, final String... expectedPart) {
    return strings -> {
      then(strings)
          .contains(expectedPart)
          .hasSize(expectedAllSize);
      return true;
    };
  }
}
