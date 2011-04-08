package com.intellij.flex.uiDesigner;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.collection.IsArray;
import org.hamcrest.core.IsEqual;

@SuppressWarnings({"unchecked"})
public final class MatcherAssert {
  public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
    assertThat("", actual, matcher);
  }

  public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
    if (!matcher.matches(actual)) {
      Description description = new StringDescription();
      description.appendText(reason)
        .appendText("\nExpected: ")
        .appendDescriptionOf(matcher)
        .appendText("\n     but: ");
      matcher.describeMismatch(actual, description);

      throw new AssertionError(description.toString());
    }
  }

  public static void assertThat(String reason, boolean assertion) {
    if (!assertion) {
      throw new AssertionError(reason);
    }
  }

  public static void assertThat(int actual, int expected) {
    assertThat(actual, new IsEqual<Integer>(expected));
  }

  public static void assertThat(String actual, String expected) {
    assertThat(actual, new IsEqual<String>(expected));
  }

  public static void assertThat(int[] actual, int... expected) {
    IsEqual[] elementMatchers = new IsEqual[expected.length];
    for (int i = 0, expectedLength = expected.length; i < expectedLength; i++) {
      elementMatchers[i] = new IsEqual<Integer>(expected[i]);
    }

    assertThat(toIntegerList(actual), new IsArray<Integer>(elementMatchers));
  }

  private static Integer[] toIntegerList(int[] array) {
    Integer[] list = new Integer[array.length];
    for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
      list[i]= array[i];
    }

    return list;
  }

  public static void assertThat(String[] actual, String... expected) {
    IsEqual[] elementMatchers = new IsEqual[expected.length];
    for (int i = 0, expectedLength = expected.length; i < expectedLength; i++) {
      elementMatchers[i] = new IsEqual<String>(expected[i]);
    }

    assertThat(actual, new IsArray<String>(elementMatchers));
  }
}
