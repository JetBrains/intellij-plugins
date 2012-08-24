package com.google.jstestdriver.idea.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Sergey Simonchik
 */
public class TestFileScope {

  private static final char TEST_CASE_SEPARATOR = ';';
  private static final char CASE_AND_METHOD_SEPARATOR = '.';
  private static final char TEST_METHOD_SEPARATOR = ',';

  private final boolean myAll;
  private final Map<String, Set<String>> myMethodsByCaseMap;

  private TestFileScope(boolean all, @Nullable Map<String, Set<String>> methodsByCaseMap) {
    myAll = all;
    if (methodsByCaseMap == null) {
      myMethodsByCaseMap = Collections.emptyMap();
    }
    else {
      myMethodsByCaseMap = methodsByCaseMap;
    }
  }

  public boolean containsTestCase(@NotNull String testCaseName) {
    return myAll || myMethodsByCaseMap.containsKey(testCaseName);
  }

  public boolean containsTestCaseAndMethod(@NotNull String testCaseName,
                                           @NotNull String testMethodName) {
    if (myAll) {
      return true;
    }
    Set<String> methods = myMethodsByCaseMap.get(testCaseName);
    if (methods == null) {
      return false;
    }
    if (methods.isEmpty()) {
      return true;
    }
    return methods.contains(testMethodName);
  }

  @Nullable
  public Map.Entry<String, Set<String>> getSingleTestCaseEntry() {
    if (myMethodsByCaseMap.isEmpty()) {
      return null;
    }
    return myMethodsByCaseMap.entrySet().iterator().next();
  }

  @NotNull
  public String toJstdScope() {
    return format(true);
  }

  @NotNull
  public String humanize() {
    return format(false);
  }

  @NotNull
  private String format(boolean asJstdScope) {
    if (myAll) {
      return "all";
    }
    StringBuilder buffer = new StringBuilder();
    for (Map.Entry<String, Set<String>> entry : myMethodsByCaseMap.entrySet()) {
      String testCaseName = entry.getKey();
      Set<String> testMethodNames = entry.getValue();
      if (testMethodNames.isEmpty()) {
        append(buffer, testCaseName, null, asJstdScope);
      }
      else {
        for (String testMethodName : entry.getValue()) {
          append(buffer, testCaseName, testMethodName, asJstdScope);
        }
      }
    }
    return buffer.toString();
  }

  public boolean isAll() {
    return myAll;
  }

  private static void append(@NotNull StringBuilder buf,
                             @NotNull String testCaseName,
                             @Nullable String testMethodName,
                             boolean asJstdScope) {
    if (buf.length() > 0) {
      buf.append(",");
    }
    if (asJstdScope) {
      buf.append('^');
    }
    buf.append(testCaseName);
    if (asJstdScope) {
      buf.append("#");
    }
    if (testMethodName != null) {
      if (!asJstdScope) {
        buf.append(".");
      }
      buf.append(testMethodName);
      if (asJstdScope) {
        buf.append('$');
      }
    }
  }

  public String serialize() {
    if (myAll) {
      throw new RuntimeException("Can't serialize for all tests");
    }
    List<String> testCaseStrings = new ArrayList<String>(myMethodsByCaseMap.size());
    for (Map.Entry<String, Set<String>> entry : myMethodsByCaseMap.entrySet()) {
      Set<String> testMethods = entry.getValue();
      if (testMethods == null) {
        testMethods = Collections.emptySet();
      }
      String testMethodsStr = EscapeUtils.join(testMethods, TEST_METHOD_SEPARATOR);
      String testCaseStr = EscapeUtils.join(Arrays.asList(entry.getKey(), testMethodsStr), CASE_AND_METHOD_SEPARATOR);
      if (!testCaseStr.isEmpty() && testCaseStr.charAt(testCaseStr.length() - 1) == CASE_AND_METHOD_SEPARATOR) {
        testCaseStr = testCaseStr.substring(0, testCaseStr.length() - 1);
      }
      testCaseStrings.add(testCaseStr);
    }
    return EscapeUtils.join(testCaseStrings, TEST_CASE_SEPARATOR);
  }

  @NotNull
  public static TestFileScope deserialize(@NotNull String s) {
    List<String> testCases = EscapeUtils.split(s, TEST_CASE_SEPARATOR);
    Map<String, Set<String>> methodsByCaseMap = new HashMap<String, Set<String>>();
    for (String testCase : testCases) {
      List<String> comps = EscapeUtils.split(testCase, CASE_AND_METHOD_SEPARATOR);
      if (comps.size() > 0) {
        String testCaseName = comps.get(0);
        final Set<String> methods;
        if (comps.size() == 1) {
          methods = Collections.emptySet();
        }
        else if (comps.size() == 2) {
          List<String> list = EscapeUtils.split(comps.get(1), TEST_METHOD_SEPARATOR);
          methods = new HashSet<String>(list);
        }
        else {
          // illegal situation
          throw new RuntimeException("Can't deserialize " + testCase);
        }
        methodsByCaseMap.put(testCaseName, methods);
      }
    }
    return new TestFileScope(false, methodsByCaseMap);
  }

  public static TestFileScope allScope() {
    return new TestFileScope(true, null);
  }

  public static TestFileScope customScope(@NotNull Map<String, Set<String>> methodsByCaseMap) {
    return new TestFileScope(false, methodsByCaseMap);
  }

}
