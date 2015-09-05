/**
 * Created with IntelliJ IDEA.
 * User: Kirill.Safonov
 * Date: 6/7/12
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
package com.intellij.flexunit.runner {

import flexunit.framework.AssertionFailedError;
import flexunit.framework.Test;
import flexunit.framework.TestCase;
import flexunit.framework.TestListener;
import flexunit.framework.TestResult;
import flexunit.framework.TestSuite;

// for FlexUnit 1
public class TestRunner1 extends TestRunnerBase implements TestListener {
    private var _suite:TestSuite;

    public function TestRunner1(port:int, socketPolicyPort:int, moduleName:String, beforeRunTests:Function) {
        super(port, socketPolicyPort, moduleName, beforeRunTests);
        _suite = new TestSuite();
    }


    override public function addTestClass(clazz:Class):void {
        _suite.addTestSuite(clazz);
    }

    override public function addTestSuiteClass(clazz:Class):void {
        _suite.addTest(new clazz());
    }

    override public function addTestMethod(clazz:Class, methodName:String):void {
        var test:TestCase = new clazz();
        test.methodName = methodName;
        _suite.addTest(test);
    }

    override protected function runTests():void {
        onRunStarted(_suite.countTestCases());

        var testResult:TestResult = new TestResult();
        testResult.addListener(this);
        _suite.runWithResult(testResult);
    }

    public function startTest(test:Test):void {
        onTestStarted(getTestInfo(test));
    }

    public function addError(test:Test, error:Error):void {
        onTestFailed(getTestInfo(test), error.message, error.getStackTrace());
    }

    public function addFailure(test:Test, error:AssertionFailedError):void {
        addError(test, error);
    }

    public function endTest(test:Test):void {
        onTestFinished(getTestInfo(test));
    }

    private static function getTestInfo(test:Test):String {
        return test.className + "." + (test as TestCase).methodName;
    }
}
}
