// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.test;

import com.google.gson.JsonSyntaxException;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.*;
import com.intellij.execution.testframework.sm.runner.events.*;
import com.intellij.execution.testframework.sm.runner.ui.MockPrinter;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.execution.testframework.ui.TestsOutputConsolePrinter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.testFramework.PlatformTestUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DartTestEventsConverterTest extends BaseSMTRunnerTestCase {

  // Do not reformat this list.
  private static final String[] DART_TEST_RUNNER_0_12_9_EVENTS = {
    // @formatter:off
    "/usr/local/opt/dart/libexec/bin/dart --checked file:///usr/local/opt/dart/libexec/bin/snapshots/pub.dart.snapshot run test:test -r json /Users/messick/src/quiver-dart/test/\n",
    "{\"protocolVersion\":\"0.1.0\",\"runnerVersion\":\"0.12.9-dev (from ../test-master)\",\"type\":\"start\",\"time\":0}\n",
    "{\"suite\":{\"id\":0,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/collect_test.dart\"},\"type\":\"suite\",\"time\":0}\n",
    "{\"test\":{\"id\":1,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/collect_test.dart\",\"suiteID\":0,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":0}\n",
    "{\"suite\":{\"id\":2,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/concat_test.dart\"},\"type\":\"suite\",\"time\":1}\n",
    "{\"test\":{\"id\":3,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/concat_test.dart\",\"suiteID\":2,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":1}\n",
    "{\"suite\":{\"id\":4,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/countdown_timer_test.dart\"},\"type\":\"suite\",\"time\":1}\n",
    "{\"test\":{\"id\":5,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/countdown_timer_test.dart\",\"suiteID\":4,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":2}\n",
    "{\"suite\":{\"id\":6,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/create_timer_test.dart\"},\"type\":\"suite\",\"time\":2}\n",
    "{\"test\":{\"id\":7,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/create_timer_test.dart\",\"suiteID\":6,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":2}\n",
    "{\"suite\":{\"id\":8,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/enumerate_test.dart\"},\"type\":\"suite\",\"time\":2}\n",
    "{\"test\":{\"id\":9,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/enumerate_test.dart\",\"suiteID\":8,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":2}\n",
    "{\"suite\":{\"id\":10,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/future_group_test.dart\"},\"type\":\"suite\",\"time\":3}\n",
    "{\"test\":{\"id\":11,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/future_group_test.dart\",\"suiteID\":10,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":3}\n",
    "{\"suite\":{\"id\":12,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/future_stream_test.dart\"},\"type\":\"suite\",\"time\":3}\n",
    "{\"test\":{\"id\":13,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/future_stream_test.dart\",\"suiteID\":12,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":4}\n",
    "{\"suite\":{\"id\":14,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/iteration_test.dart\"},\"type\":\"suite\",\"time\":4}\n",
    "{\"test\":{\"id\":15,\"name\":\"compiling /Users/messick/src/quiver-dart/test/async/iteration_test.dart\",\"suiteID\":14,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":4}\n",
    "{\"testID\":5,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":723}\n",
    "{\"group\":{\"id\":16,\"suiteID\":4,\"parentID\":null,\"name\":null,\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":730}\n",
    "{\"group\":{\"id\":17,\"suiteID\":4,\"parentID\":16,\"name\":\"CountdownTimer\",\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":730}\n",
    "{\"test\":{\"id\":18,\"name\":\"CountdownTimer should countdown\",\"suiteID\":4,\"groupIDs\":[16,17],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":732}\n",
    "{\"count\":11,\"type\":\"allSuites\",\"time\":732}",
    "{\"testID\":1,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":733}\n",
    "{\"group\":{\"id\":19,\"suiteID\":0,\"parentID\":null,\"name\":null,\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":733}\n",
    "{\"group\":{\"id\":20,\"suiteID\":0,\"parentID\":19,\"name\":\"collect\",\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":734}\n",
    "{\"test\":{\"id\":21,\"name\":\"collect should produce no events for no futures\",\"suiteID\":0,\"groupIDs\":[19,20],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":734}\n",
    "{\"testID\":7,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":734}\n",
    "{\"group\":{\"id\":22,\"suiteID\":6,\"parentID\":null,\"name\":null,\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":734}\n",
    "{\"group\":{\"id\":23,\"suiteID\":6,\"parentID\":22,\"name\":\"createTimer\",\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":734}\n",
    "{\"test\":{\"id\":24,\"name\":\"createTimer should be assignable to CreateTimer\",\"suiteID\":6,\"groupIDs\":[22,23],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":735}\n",
    "{\"testID\":11,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":735}\n",
    "{\"group\":{\"id\":25,\"suiteID\":10,\"parentID\":null,\"name\":null,\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":735}\n",
    "{\"group\":{\"id\":26,\"suiteID\":10,\"parentID\":25,\"name\":\"FutureGroup\",\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":735}\n",
    "{\"test\":{\"id\":27,\"name\":\"FutureGroup should complete when all added futures are complete\",\"suiteID\":10,\"groupIDs\":[25,26],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":735}\n",
    "{\"testID\":9,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":737}\n",
    "{\"testID\":3,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":748}\n",
    "{\"testID\":15,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":751}\n",
    "{\"testID\":13,\"result\":\"success\",\"hidden\":true,\"type\":\"testDone\",\"time\":755}\n",
    "{\"testID\":24,\"result\":\"success\",\"hidden\":false,\"type\":\"testDone\",\"time\":773}\n",
    "{\"group\":{\"id\":28,\"suiteID\":6,\"parentID\":22,\"name\":\"createTimerPeriodic\",\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":774}\n",
    "{\"test\":{\"id\":29,\"name\":\"createTimerPeriodic should be assignable to CreateTimerPeriodic\",\"suiteID\":6,\"groupIDs\":[22,28],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":774}\n",
    "{\"testID\":21,\"result\":\"success\",\"hidden\":false,\"type\":\"testDone\",\"time\":776}\n",
    "{\"test\":{\"id\":30,\"name\":\"collect should produce events for future completions in input order\",\"suiteID\":0,\"groupIDs\":[19,20],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":777}\n",
    "{\"testID\":29,\"result\":\"success\",\"hidden\":false,\"type\":\"testDone\",\"time\":794}\n",
    "{\"suite\":{\"id\":31,\"platform\":\"vm\",\"path\":\"/Users/messick/src/quiver-dart/test/async/metronome_test.dart\"},\"type\":\"suite\",\"time\":798}\n",
    "{\"test\":{\"id\":32,\"name\":\"loading /Users/messick/src/quiver-dart/test/async/metronome_test.dart\",\"suiteID\":31,\"groupIDs\":[],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":798}\n",
    "{\"group\":{\"id\":33,\"suiteID\":8,\"parentID\":null,\"name\":null,\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":798}\n",
    "{\"group\":{\"id\":34,\"suiteID\":8,\"parentID\":33,\"name\":\"enumerate\",\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"group\",\"time\":798}\n",
    "{\"test\":{\"id\":35,\"name\":\"enumerate should add indices to its argument\",\"suiteID\":8,\"groupIDs\":[33,34],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":799}\n",
    "{\"testID\":27,\"result\":\"success\",\"hidden\":false,\"type\":\"testDone\",\"time\":799}\n",
    "{\"test\":{\"id\":36,\"name\":\"FutureGroup should throw if adding a future after the group is completed\",\"suiteID\":10,\"groupIDs\":[25,26],\"metadata\":{\"skip\":false,\"skipReason\":null}},\"type\":\"testStart\",\"time\":801}\n",
    "{\"testID\":36,\"result\":\"success\",\"hidden\":false,\"type\":\"testDone\",\"time\":805}\n",
    "{\"success\":true,\"type\":\"done\",\"time\":4877}\n",
    "\n",
    "Process finished with exit code 1\n",
    // @formatter:on
  };

  private static final String[] DART_TEST_RUNNER_0_12_9_SIGNALS = {
    "suite started countdown_timer_test.dart",
    "suite started CountdownTimer",
    "start should countdown",
    "suite started collect_test.dart",
    "suite started collect",
    "start should produce no events for no futures",
    "suite started create_timer_test.dart",
    "suite started createTimer",
    "start should be assignable to CreateTimer",
    "suite started future_group_test.dart",
    "suite started FutureGroup",
    "start should complete when all added futures are complete",
    "finish should be assignable to CreateTimer",
    "suite started createTimerPeriodic",
    "start should be assignable to CreateTimerPeriodic",
    "finish should produce no events for no futures",
    "start should produce events for future completions in input order",
    "finish should be assignable to CreateTimerPeriodic",
    "suite started enumerate_test.dart",
    "suite started enumerate",
    "start should add indices to its argument",
    "finish should complete when all added futures are complete",
    "start should throw if adding a future after the group is completed",
    "finish should throw if adding a future after the group is completed",
    "suite finished countdown_timer_test.dart",
    "suite finished CountdownTimer",
    "suite finished enumerate_test.dart",
    "suite finished enumerate",
    "suite finished collect_test.dart",
    "suite finished collect",
    "suite finished create_timer_test.dart",
    "suite finished createTimer",
    "suite finished future_group_test.dart",
    "suite finished FutureGroup",
    "suite finished createTimerPeriodic"
  };

  private SMTRunnerConsoleView myConsole;
  private DartTestEventsConverter myEventsConverter;
  private DartTestEventsProcessor myEventsProcessor;
  private DefaultMutableTreeNode myParentNode;
  private SMTestRunnerResultsForm myResultsViewer;
  private MockPrinter myMockResettablePrinter;
  private Map<String, DefaultMutableTreeNode> myNodes;

  public void testSample() {
    Map<Integer, Integer> parents = new HashMap<>();
    // Group relationships
    parents.put(17, 16);
    parents.put(20, 19);
    parents.put(23, 22);
    parents.put(26, 25);
    parents.put(28, 22);
    parents.put(34, 33);

    // Test relationships
    parents.put(18, 17);
    parents.put(21, 20);
    parents.put(24, 23);
    parents.put(27, 26);
    parents.put(29, 28);
    parents.put(30, 20);
    parents.put(35, 34);
    parents.put(36, 26);

    runTest(DART_TEST_RUNNER_0_12_9_EVENTS, DART_TEST_RUNNER_0_12_9_SIGNALS, parents);
  }

  public void testLoadFailure() {
    String[] events = {
      "{'test':{'id':0,'name':'loading test/formatter_test.dart','groupIDs':[],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':0}\n",
      "{'testID':0,'error':'Failed to load \\\"test/formatter_test.dart\\\":\\nline 117 pos 69','stackTrace':'','isFailure':false,'type':'error','time':497}\n",
      "{'testID':0,'error':'2nd failure message','stackTrace':'','isFailure':false,'type':'error','time':497}\n",
      "{'testID':0,'error':'3rd failure message','stackTrace':'3rd stack trace','isFailure':false,'type':'error','time':497}\n",
      "{'testID':0,'result':'error','hidden':false,'type':'testDone','time':499}\n",
      "{'test':{'id':1,'name':'loading another_bad_test.dart','groupIDs':[],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':0}\n",
      "{'testID':1,'message':'some output\\n','type':'print','time':30}\n",
      "{'testID':1,'error':'Failed to load \\\"another_bad_test.dart\\\"','stackTrace':'some stack','isFailure':false,'type':'error','time':497}\n",
      "{'testID':1,'result':'error','hidden':true,'type':'testDone','time':499}\n",
      "{'success':false,'type':'done','time':500}\n", "\n", "Process finished with exit code 1\n",
    };
    String[] signals = {
      "start loading formatter_test.dart",
      "fail loading formatter_test.dart false",
      "print loading formatter_test.dart 2nd failure message\n",
      "print loading formatter_test.dart 3rd failure message\n",
      "print loading formatter_test.dart 3rd stack trace\n",
      "finish loading formatter_test.dart",
      "start loading another_bad_test.dart",
      "print loading another_bad_test.dart some output\n",
      "fail loading another_bad_test.dart false",
      "print loading another_bad_test.dart some stack\n",
      "finish loading another_bad_test.dart"
    };
    runTest(events, signals, Map.of());
  }

  public void testSetUpAllFailure() {
    String[] events = {
      "{'count':1,'type':'allSuites','time':0}\n",
      "{'suite':{'id':0,'platform':'vm','path':'test/next_test.dart'},'type':'suite','time':0}\n",
      "{'test':{'id':1,'name':'loading test/next_test.dart','suiteID':0,'groupIDs':[],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':0}\n",
      "{'testID':1,'result':'success','hidden':true,'type':'testDone','time':234}\n",
      "{'group':{'id':2,'suiteID':0,'parentID':null,'name':null,'metadata':{'skip':false,'skipReason':null},'testCount':5},'type':'group','time':238}\n",
      "{'test':{'id':3,'name':'(setUpAll)','suiteID':0,'groupIDs':[2],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':238}\n",
      "{'testID':3,'message':'sa2','type':'print','time':258}\n",
      "{'testID':3,'error':'fail','stackTrace':'test/next_test.dart 4:30  main.<fn>','isFailure':false,'type':'error','time':389}\n",
      "{'testID':3,'result':'error','hidden':false,'type':'testDone','time':390}\n",
      "{'success':false,'type':'done','time':392}\n",
    };
    String[] signals = {
      "suite started next_test.dart",
      "start (setUpAll)",
      "fail (setUpAll) false",
      "print (setUpAll) test/next_test.dart 4:30  main.<fn>\n",
      "finish (setUpAll)",
      "suite finished next_test.dart"
    };

    runTest(events, signals, Map.of());
  }

  public void testGroupsDone() {
    String[] events = {
      "{'protocolVersion':'0.1.0','runnerVersion':'0.12.10','type':'start','time':0}\n",
      "{'count':1,'type':'allSuites','time':0}\n",
      "{'suite':{'id':0,'platform':'vm','path':'C:/dart_projects/DartSample2/test/a/bb/c/foo_test.dart'},'type':'suite','time':0}\n",
      "{'test':{'id':1,'name':'loading C:/dart_projects/DartSample2/test/a/bb/c/foo_test.dart','suiteID':0,'groupIDs':[],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':0}\n",
      "{'testID':1,'result':'success','hidden':true,'type':'testDone','time':192}\n",
      "{'group':{'id':2,'suiteID':0,'parentID':null,'name':null,'metadata':{'skip':false,'skipReason':null},'testCount':6},'type':'group','time':196}\n",
      "{'group':{'id':3,'suiteID':0,'parentID':2,'name':'some group','metadata':{'skip':false,'skipReason':null},'testCount':5},'type':'group','time':196}\n",
      "{'group':{'id':4,'suiteID':0,'parentID':3,'name':'some group sub group 1','metadata':{'skip':false,'skipReason':null},'testCount':1},'type':'group','time':196}\n",
      "{'test':{'id':30,'name':'some group sub group 1 (setUpAll)','suiteID':0,'groupIDs':[2,3,4],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':196}\n",
      "{'testID':30,'message':'foo','type':'print','time':258}\n",
      "{'testID':30,'result':'success','hidden':true,'type':'testDone','time':261}\n",
      "{'test':{'id':5,'name':'some group sub group 1 passing test1','suiteID':0,'groupIDs':[2,3,4],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':196}\n",
      "{'testID':5,'result':'success','hidden':false,'type':'testDone','time':1224}\n",
      "{'group':{'id':6,'suiteID':0,'parentID':3,'name':'some group sub group 2','metadata':{'skip':false,'skipReason':null},'testCount':3},'type':'group','time':1224}\n",
      "{'test':{'id':7,'name':'some group sub group 2 passing test 2','suiteID':0,'groupIDs':[2,3,6],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':1224}\n",
      "{'testID':7,'result':'success','hidden':false,'type':'testDone','time':2226}\n",
      "{'test':{'id':8,'name':'some group sub group 2 skipped test','suiteID':0,'groupIDs':[2,3,6],'metadata':{'skip':true,'skipReason':'skip reason'}},'type':'testStart','time':2227}\n",
      "{'testID':8,'result':'success','hidden':false,'type':'testDone','time':2227}\n",
      "{'test':{'id':9,'name':'some group sub group 2 standard TestFailure','suiteID':0,'groupIDs':[2,3,6],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':2228}\n",
      "{'testID':9,'message':'in standard TestFailure test','type':'print','time':2230}\n",
      "{'testID':9,'error':'Expected: <false>\n  Actual: <true>\n','stackTrace':'package:test                     expect\ntest\\a\\bb\\c\\foo_test.dart 12:79  main.<fn>.<fn>.<fn>\n','isFailure':true,'type':'error','time':2365}\n",
      "{'testID':9,'result':'failure','hidden':false,'type':'testDone','time':2366}\n",
      "{'test':{'id':10,'name':'some group unexpected error','suiteID':0,'groupIDs':[2,3],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':2366}\n",
      "{'testID':10,'message':'in unexpected error test','type':'print','time':2367}\n",
      "{'testID':10,'error':'Unlucky','stackTrace':'test\\a\\bb\\c\\foo_test.dart 15:69  main.<fn>.<fn>\n','isFailure':false,'type':'error','time':2401}\n",
      "{'testID':10,'result':'error','hidden':false,'type':'testDone','time':2401}\n",
      "{'test':{'id':11,'name':'passing test 3','suiteID':0,'groupIDs':[2],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':2402}\n",
      "{'testID':11,'message':'in passing test','type':'print','time':2403}\n",
      "{'testID':11,'result':'success','hidden':false,'type':'testDone','time':3404}\n",
      "{'test':{'id':50,'name':'(tearDownAll)','suiteID':0,'groupIDs':[2],'metadata':{'skip':false,'skipReason':null}},'type':'testStart','time':267}\n",
      "{'testID':50,'message':'tda','type':'print','time':269}\n",
      "{'testID':50,'result':'success','hidden':true,'type':'testDone','time':270}\n",
      "{'success':false,'type':'done','time':3406}\n",
    };
    String[] signals = {
      "suite started foo_test.dart",
      "suite started some group",
      "suite started sub group 1",
      "start passing test1",
      "finish passing test1",
      "suite finished sub group 1",
      "suite started sub group 2",
      "start passing test 2",
      "finish passing test 2",
      "start skipped test",
      "skip skipped test skip reason",
      "finish skipped test",
      "start standard TestFailure",
      "print standard TestFailure in standard TestFailure test\n",
      "finish standard TestFailure",
      "suite finished sub group 2",
      "start unexpected error",
      "print unexpected error in unexpected error test\n",
      "finish unexpected error",
      "suite finished some group",
      "start passing test 3",
      "print passing test 3 in passing test\n",
      "finish passing test 3",
      "suite finished foo_test.dart"
    };

    runTest(events, signals, Map.of());
  }

  private void runTest(String[] jsonEvents, String[] expectedSignals, Map<Integer, Integer> parents) {
    DartTestEventsConverter parser = myEventsConverter;
    Key key = new Key("stdout");
    for (String event : jsonEvents) {
      try {
        parser.process(event, key);
      }
      catch (JsonSyntaxException ex) {
        // ignored
      }
    }

    assertOrderedEquals(myEventsProcessor.signals, expectedSignals);

    for (Map.Entry<Integer, Integer> entry : parents.entrySet()) {
      int childIdx = entry.getKey();
      int parentIdx = entry.getValue();
      if (parentIdx > 0) {
        DefaultMutableTreeNode child = myNodes.get(String.valueOf(childIdx));
        DefaultMutableTreeNode parent = myNodes.get(String.valueOf(parentIdx));
        assertEquals(parent, child.getParent());
      }
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myNodes = new HashMap<>();
    myMockResettablePrinter = new MockPrinter();
    TestConsoleProperties consoleProperties = createConsoleProperties();
    myConsole = new MyConsoleView(consoleProperties);
    myConsole.initUI();
    myResultsViewer = myConsole.getResultsViewer();
    myEventsConverter = new DartTestEventsConverter(DartTestRunningState.DART_FRAMEWORK_NAME, consoleProperties,
                                                    DartUrlResolver.getInstance(getProject(), getSourceRoot()));
    myEventsProcessor = new DartTestEventsProcessor(consoleProperties.getProject(), DartTestRunningState.DART_FRAMEWORK_NAME);
    myEventsProcessor.addEventsListener(myResultsViewer);
    myEventsConverter.setProcessor(myEventsProcessor);
    TreeModel treeModel = myResultsViewer.getTreeView() == null ? null : myResultsViewer.getTreeView().getModel();
    assertNotNull(treeModel);
    PlatformTestUtil.waitWhileBusy(myResultsViewer.getTreeView());
    myParentNode = (DefaultMutableTreeNode)treeModel.getRoot();
    myEventsProcessor.onStartTesting();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      Disposer.dispose(myEventsProcessor);
      Disposer.dispose(myConsole);
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      super.tearDown();
    }
  }

  @Override
  protected TestConsoleProperties createConsoleProperties() {
    TestConsoleProperties properties = super.createConsoleProperties();
    TestConsoleProperties.HIDE_PASSED_TESTS.set(properties, false);
    TestConsoleProperties.OPEN_FAILURE_LINE.set(properties, false);
    TestConsoleProperties.SCROLL_TO_SOURCE.set(properties, false);
    TestConsoleProperties.SELECT_FIRST_DEFECT.set(properties, false);
    TestConsoleProperties.TRACK_RUNNING_TEST.set(properties, false);
    return properties;
  }

  private final class MyConsoleView extends SMTRunnerConsoleView {
    private final TestsOutputConsolePrinter myTestsOutputConsolePrinter;

    private MyConsoleView(final TestConsoleProperties consoleProperties) {
      super(consoleProperties);

      myTestsOutputConsolePrinter = new TestsOutputConsolePrinter(this, consoleProperties, null) {
        @Override
        public void print(final @NotNull String text, final @NotNull ConsoleViewContentType contentType) {
          myMockResettablePrinter.print(text, contentType);
        }
      };
    }

    @Override
    public TestsOutputConsolePrinter getPrinter() {
      return myTestsOutputConsolePrinter;
    }
  }

  private class DartTestEventsProcessor extends GeneralTestEventsProcessor {
    List<String> signals = new ArrayList<>();

    DartTestEventsProcessor(Project project, @NotNull String testFrameworkName) {
      super(project, testFrameworkName, new SMTestProxy.SMRootTestProxy());
    }

    @Override
    public void onStartTesting() {
      myResultsViewer.performUpdate();
    }

    @Override
    public void onTestsCountInSuite(int count) {
    }

    @Override
    public void onTestStarted(@NotNull TestStartedEvent testStartedEvent) {
      signals.add("start " + testStartedEvent.getName());
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(testStartedEvent.getName());
      myNodes.put(testStartedEvent.getId(), node);
      DefaultMutableTreeNode parentNode = myParentNode;
      String parentId = testStartedEvent.getParentId();
      if (parentId != null && !parentId.equals(TreeNodeEvent.ROOT_NODE_ID)) {
        parentNode = myNodes.get(parentId);
      }
      node.setParent(parentNode);
      myResultsViewer.performUpdate();
    }

    @Override
    public void onTestFinished(@NotNull TestFinishedEvent testFinishedEvent) {
      signals.add("finish " + testFinishedEvent.getName());
    }

    @Override
    public void onTestFailure(@NotNull TestFailedEvent testFailedEvent) {
      signals.add("fail " + testFailedEvent.getName() + " " + testFailedEvent.isTestError());
    }

    @Override
    public void onTestIgnored(@NotNull TestIgnoredEvent testIgnoredEvent) {
      signals.add("skip " + testIgnoredEvent.getName() + " " + testIgnoredEvent.getIgnoreComment());
    }

    @Override
    public void onTestOutput(@NotNull TestOutputEvent testOutputEvent) {
      signals.add("print " + testOutputEvent.getName() + " " + testOutputEvent.getText());
    }

    @Override
    public void onSuiteStarted(@NotNull TestSuiteStartedEvent suiteStartedEvent) {
      signals.add("suite started " + suiteStartedEvent.getName());

      DefaultMutableTreeNode node = new DefaultMutableTreeNode(suiteStartedEvent.getName());
      myNodes.put(suiteStartedEvent.getId(), node);
      DefaultMutableTreeNode parentNode = myParentNode;
      String parentId = suiteStartedEvent.getParentId();
      if (parentId != null && !parentId.equals(TreeNodeEvent.ROOT_NODE_ID)) {
        parentNode = myNodes.get(parentId);
      }
      node.setParent(parentNode);
      myResultsViewer.performUpdate();
    }

    @Override
    public void onSuiteFinished(@NotNull TestSuiteFinishedEvent suiteFinishedEvent) {
      signals.add("suite finished " + suiteFinishedEvent.getName());
    }

    @Override
    public void onUncapturedOutput(@NotNull String text, Key outputType) {
    }

    @Override
    public void onError(@NotNull String localizedMessage, @Nullable String stackTrace, boolean isCritical) {
      signals.add("error " + localizedMessage + " " + stackTrace);
    }

    @Override
    public void onTestsReporterAttached() {
    }

    @Override
    public void setLocator(@NotNull SMTestLocator locator) {
    }

    @Override
    public void setPrinterProvider(@NotNull TestProxyPrinterProvider printerProvider) {
    }
  }
}
