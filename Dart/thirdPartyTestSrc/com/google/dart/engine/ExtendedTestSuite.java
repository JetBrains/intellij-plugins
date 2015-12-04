/*
 * Copyright (c) 2012, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.engine;

import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Instances of the class {@code ExtendedTestSuite} implement a test suite that will run both normal
 * and failing tests. Normal tests are {@code public}, zero-argument, {@code void} methods whose
 * name starts with {@code "test"}. Failing tests are {@code public}, zero-argument, {@code void}
 * methods whose name starts with {@code "fail"}. Failing tests pass if they throw an exception and
 * fail if they do not.
 */
public class ExtendedTestSuite extends TestSuite {
  /**
   * Instances of the class {@code InvertingTestResult} wrap an existing test result to invert the
   * expectation for a test that is about to be run.
   */
  private static class InvertingTestResult extends TestResult {
    /**
     * The test result being wrapped by this test result.
     */
    private TestResult wrappedResult;

    /**
     * Initialize a newly created test result to report any failure to the given test result.
     * 
     * @param wrappedResult the test result being wrapped by this test result
     */
    public InvertingTestResult(TestResult wrappedResult) {
      this.wrappedResult = wrappedResult;
    }

    @Override
    public void endTest(Test test) {
      wrappedResult.endTest(test);
    }

    @Override
    public void runProtected(Test test, Protectable protectable) {
      try {
        protectable.protect();
      } catch (ThreadDeath exception) { // don't catch ThreadDeath by accident
        throw exception;
        // Would like to use the more specific JUnit [AssertionFailedError] here, but
        // some tests (e.g. [SearchEngineImplTest]) use the [org.fest.assertions]
        // package, which throws plain Java [AssertionError].
      } catch (AssertionError exception) {
        return;
      } catch (Throwable exception) { // Unexpected exceptions are still failures.
        wrappedResult.addFailure(test, new AssertionFailedError(
            "Expected assertion failure, but got other failure: " + exception));
        return;
      }
      wrappedResult.addFailure(test, new AssertionFailedError(
          "Expected assertion failure, but passed"));
    }

    @Override
    public void startTest(Test test) {
      wrappedResult.startTest(test);
    }
  }

  /**
   * Initialize a newly created test suite to include all of the normal and failing tests defined by
   * the given class.
   * 
   * @param testClass the class defining the test methods
   */
  public ExtendedTestSuite(Class<? extends TestCase> testClass) {
    super(testClass);
    addFailingTestsFromTestCase(testClass);
  }

  /**
   * Initialize a newly created test suite to be empty but have the given name.
   * 
   * @param name the name of the test suite
   */
  public ExtendedTestSuite(String name) {
    super(name);
  }

  @Override
  public void addTestSuite(Class<? extends TestCase> testClass) {
    addTest(new ExtendedTestSuite(testClass));
  }

  @Override
  public void runTest(Test test, TestResult result) {
    if (test instanceof TestCase && ((TestCase) test).getName().startsWith("fail")) {
      test.run(new InvertingTestResult(result));
    } else {
      test.run(result);
    }
  }

  private void addFailingTestMethod(Method method, List<String> names, Class<?> testClass) {
    String name = method.getName();
    if (names.contains(name)) {
      return;
    }
    if (!isPublicFailingTestMethod(method)) {
      if (isFailingTestMethod(method)) {
        addTest(warning("Test method isn't public: " + method.getName() + "("
            + testClass.getCanonicalName() + ")"));
      }
      return;
    }
    names.add(name);
    addTest(createTest(testClass, name));
  }

  private void addFailingTestsFromTestCase(final Class<?> testClass) {
    //
    // These conditions were tested and reported by the superclass. They are re-tested here to avoid
    // reporting them multiple times.
    //
    try {
      getTestConstructor(testClass);
    } catch (NoSuchMethodException exception) {
      return;
    }
    if (!Modifier.isPublic(testClass.getModifiers())) {
      return;
    }
    //
    // Add the actual tests.
    //
    Class<?> superclass = testClass;
    List<String> names = new ArrayList<String>();
    while (Test.class.isAssignableFrom(superclass)) {
      for (Method each : superclass.getDeclaredMethods()) {
        addFailingTestMethod(each, names, testClass);
      }
      superclass = superclass.getSuperclass();
    }
  }

  private boolean isFailingTestMethod(Method method) {
    return method.getParameterTypes().length == 0 && method.getName().startsWith("fail")
        && method.getReturnType().equals(Void.TYPE);
  }

  private boolean isPublicFailingTestMethod(Method method) {
    return isFailingTestMethod(method) && Modifier.isPublic(method.getModifiers());
  }
}
