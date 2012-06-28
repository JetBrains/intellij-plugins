/**
 * Created with IntelliJ IDEA.
 * User: Kirill.Safonov
 * Date: 6/7/12
 * Time: 9:58 PM
 * To change this template use File | Settings | File Templates.
 */
package com.intellij.flexunit.runner {
import org.flexunit.runner.FlexUnitCore;
import org.flexunit.runner.IDescription;
import org.flexunit.runner.Request;
import org.flexunit.runner.Result;
import org.flexunit.runner.notification.Failure;
import org.flexunit.runner.notification.IRunListener;

// for FlexUnit 4
public class TestRunner4 extends TestRunnerBase implements IRunListener {
    private var _suite: Array = new Array();

    public function TestRunner4(port:int, socketPolicyPort:int, moduleName:String, beforeRunTests:Function) {
        super(port, socketPolicyPort, moduleName, beforeRunTests);
    }

    override public virtual function addTestClass(clazz:Class):void {
        _suite.push(Request.aClass(clazz));
    }

    override public virtual function addTestSuiteClass(clazz:Class):void {
        addTestClass(clazz);
    }

    override public virtual function addTestMethod(clazz:Class, methodName:String):void {
        _suite.push(Request.method(clazz, methodName));
    }

    override protected virtual function runTests():void {
        var core : FlexUnitCore = new FlexUnitCore();
        core.addListener(this);
        core.run(_suite);
    }

    public function testRunStarted(description:IDescription):void {
        onRunStarted(description.testCount);
    }

    public function testRunFinished(result:Result):void {
        onTestRunFinished();
    }

    public function testStarted(description:IDescription):void {
        onTestStarted(description.displayName);
    }

    public function testFinished(description:IDescription):void {
        onTestFinished(description.displayName);
    }

    public function testFailure(failure:Failure):void {
        onTestFailed(failure.description.displayName, failure.message, failure.stackTrace);
    }

    public function testAssumptionFailure(failure:Failure):void {
        testFailure(failure);
    }

    public function testIgnored(description:IDescription):void {
        onTestIgnored(description.displayName);
    }
}
}
