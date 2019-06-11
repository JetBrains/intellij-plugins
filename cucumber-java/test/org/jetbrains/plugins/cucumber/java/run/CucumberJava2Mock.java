// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.util.ArrayUtilRt;
import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.TestStep;
import cucumber.api.event.*;
import cucumber.runner.PickleTestStep;
import cucumber.runner.UnskipableStep;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.HookDefinitionMatch;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CucumberJava2Mock implements EventPublisher {
  private final Map<Class, EventHandler> myMap = new HashMap<>();

  @Override
  public <T extends Event> void registerHandlerFor(Class<T> aClass, EventHandler<T> handler) {
    myMap.put(aClass, handler);
  }

  private static TestStep createHookStep(Method hookMethod)
    throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
    Class clazz = Class.forName("cucumber.runtime.java.JavaHookDefinition");
    Constructor constructor = clazz.getConstructors()[0];
    constructor.setAccessible(true);
    HookDefinition hookDefinition = (HookDefinition)constructor.newInstance(hookMethod, ArrayUtilRt.EMPTY_STRING_ARRAY, 0, 0L, null);

    DefinitionMatch definitionMatch = new HookDefinitionMatch(hookDefinition);
    return new UnskipableStep(HookType.Before, definitionMatch);
  }

  private static TestStep createTestStep(String name, int line, String text) {
    return new PickleTestStepMock(name, line, text);
  }

  public void simulateRun() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException,
                                   NoSuchMethodException {

    EventHandler<TestCaseStarted> testCaseStarted = myMap.get(TestCaseStarted.class);
    EventHandler<TestCaseFinished> testCaseFinished = myMap.get(TestCaseFinished.class);
    EventHandler<TestStepStarted> testStepStarted = myMap.get(TestStepStarted.class);
    EventHandler<WriteEvent> writeEvent = myMap.get(WriteEvent.class);
    EventHandler<TestStepFinished> testStepFinished = myMap.get(TestStepFinished.class);
    EventHandler<TestRunFinished> testRunFinished = myMap.get(TestRunFinished.class);

    Method hookMethod = CucumberJava2Mock.class.getMethod("simulateRun");

    TestStep hookStep = createHookStep(hookMethod);
    TestStep passingStep = createTestStep("passing", 3, "passing step");
    TestStep failingStep = createTestStep("failing", 4, "failing step");
    TestStep specialStep = createTestStep("failing", 5, "\\bstep with \"special' symbols");

    PickleLocation pickleLocation = new PickleLocation(1, 1);
    List<PickleStep> pickleSteps = Arrays.asList(passingStep.getPickleStep(), failingStep.getPickleStep(), specialStep.getPickleStep());
    Pickle pickle = new Pickle("scenario", null, pickleSteps, null, Collections.singletonList(pickleLocation));
    PickleEvent pickleEvent = new PickleEvent("feature'", pickle);

    Result resultPassed = new Result(Result.Type.PASSED, 0L, null);
    Result resultFailed = new Result(Result.Type.FAILED, 0L, null);

    //noinspection deprecation
    TestCase testCase = new TestCase(null, pickleEvent);
    testCaseStarted.receive(new TestCaseStarted(0L, testCase));

    testStepStarted.receive(new TestStepStarted(0L, hookStep));
    testStepFinished.receive(new TestStepFinished(0L, hookStep, resultPassed));

    testStepStarted.receive(new TestStepStarted(0L, passingStep));
    testStepFinished.receive(new TestStepFinished(0L, passingStep, resultPassed));

    testStepStarted.receive(new TestStepStarted(0L, failingStep));
    testStepFinished.receive(new TestStepFinished(0L, failingStep, resultFailed));

    testStepStarted.receive(new TestStepStarted(0L, specialStep));
    writeEvent.receive(new WriteEvent(0L, "text"));
    testStepFinished.receive(new TestStepFinished(0L, specialStep, resultPassed));

    testCaseFinished.receive(new TestCaseFinished(0L, testCase, resultFailed));
    testRunFinished.receive(new TestRunFinished(0L));
  }

  private static class PickleTestStepMock extends PickleTestStep {
    private final int myLine;

    private final String myText;

    PickleTestStepMock(String uri, int line, String text) {
      super(uri, null, null);
      myLine = line;
      myText = text;
    }

    @Override
    public String getStepLocation() {
      return "/features/my.feature";
    }

    @Override
    public int getStepLine() {
      return myLine;
    }

    @Override
    public String getStepText() {
      return myText;
    }
  }
}
