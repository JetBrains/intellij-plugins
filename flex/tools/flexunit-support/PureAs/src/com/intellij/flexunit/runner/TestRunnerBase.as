/**
 * Created with IntelliJ IDEA.
 * User: Kirill.Safonov
 * Date: 6/7/12
 * Time: 7:17 PM
 * To change this template use File | Settings | File Templates.
 */
package com.intellij.flexunit.runner {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IOErrorEvent;
import flash.events.ProgressEvent;
import flash.events.SecurityErrorEvent;
import flash.net.Socket;
import flash.system.Security;
import flash.system.fscommand;
import flash.utils.getDefinitionByName;
import flash.utils.getTimer;
import flash.utils.setTimeout;

import mx.utils.StringUtil;

[Event(name="statusMessage", type="com.intellij.flexunit.runner.UpdateTextEvent")]
[Event(name="mainMessage", type="com.intellij.flexunit.runner.UpdateTextEvent")]
[Event(name="failure", type="com.intellij.flexunit.runner.FailureEvent")]

public class TestRunnerBase extends EventDispatcher {

    public static const STATUS_MESSAGE:String = "statusMessage";
    public static const MAIN_MESSAGE:String = "mainMessage";

    private static const LOCATION_PROTOCOL:String = "flex_qn://";
    private static const WARNING_TEST_CASE:String = "com.intellij.flexunit.framework::WarningTestCase";
    private static const NO_TESTS_FOUND_WARNING_PREFIX:String = "Warning: No tests found in ";

    private var _socket:Socket = new Socket();

    private var _totalCount:Number;
    private var _currentCount:Number = 0;
    private var _currentSuiteName:String = null;
    private var _testStartedMs:Number;
    private var _moduleName:String;

    private var _dataPort:int;
    private var _socketPolicyPort:int;
    private var _beforeRunTests:Function;

    function TestRunnerBase(port:int, socketPolicyPort:int, moduleName:String, beforeRunTests:Function) {
        _dataPort = port;
        _socketPolicyPort = socketPolicyPort;
        _moduleName = moduleName;
        _beforeRunTests = beforeRunTests;
    }

    public function run():void {
        _socket.addEventListener(Event.CLOSE, socketClosed);
        _socket.addEventListener(Event.CONNECT, socketConnected);
        _socket.addEventListener(IOErrorEvent.IO_ERROR, socketError);
        _socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, socketSecurityError);
        _socket.addEventListener(ProgressEvent.SOCKET_DATA, socketData);

        try {
            var url:String = "xmlsocket://localhost:" + _socketPolicyPort;
            Security.loadPolicyFile(url);
            _socket.connect("localhost", _dataPort);
        } catch (e:Error) {
            connectionFailure(e.message);
        }

        dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, "Connecting to IDEA..."));
    }

    public virtual function addTestClass(clazz:Class):void {
        throw new Error("not implemented");
    }

    public virtual function addTestSuiteClass(clazz:Class):void {
        throw new Error("not implemented");
    }

    public virtual function addTestMethod(clazz:Class, methodName:String):void {
        throw new Error("not implemented");
    }

    protected virtual function runTests():void {
        throw new Error("not implemented");
    }

    protected function onRunStarted(testCount:int):void {
        _totalCount = testCount;
        traceCommand("testCount", "count", _totalCount.toString());

        if (_totalCount == 0) {
            onTestRunFinished();
        }
    }

    protected function onTestStarted(name:String):void {
        dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, StringUtil.substitute("Executing tests: {0} of {1}", _currentCount + 1, _totalCount)));
        changeCurrentSuite(name);

        if (_currentSuiteName != null && _currentSuiteName.search(WARNING_TEST_CASE) == -1) {
            var className:String = getClassName(name);
            var methodName:String = getMethodName(name);
            traceCommand("testStarted", "name", methodName, "locationHint", LOCATION_PROTOCOL + _moduleName + ":" + className + "." + methodName + "()");
        }
        _testStartedMs = getTimer();
    }

    protected function onTestFinished(name:String):void {
        if (_currentSuiteName != null && _currentSuiteName.search(WARNING_TEST_CASE) == -1) {
            traceCommand("testFinished", "name", getMethodName(name), "duration", (getTimer() - _testStartedMs).toString());
        }
        if (++_currentCount == _totalCount) {
            changeCurrentSuite(null);
            dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, "Terminating..."));
            onTestRunFinished();
        }
    }


    protected function onTestIgnored(name:String):void {
        dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, StringUtil.substitute("Executing tests: {0} of {1}", _currentCount + 1, _totalCount)));
        changeCurrentSuite(name);

        var className:String = getClassName(name);
        var methodName:String = getMethodName(name);
        traceCommand("testStarted", "name", methodName, "locationHint", LOCATION_PROTOCOL + _moduleName + ":" + className + "." + methodName + "()");
        traceCommand("testIgnored", "name", methodName, "message", "");
        traceCommand("testFinished", "name", methodName);
    }


    protected function onTestFailed(name:String, message:String, stackTrace:String):void {
        if (_currentSuiteName != null && _currentSuiteName.search(WARNING_TEST_CASE) == -1) {
            traceCommand("testFailed", "name", getMethodName(name), "message", message, "details", stackTrace);
        }
    }


    protected function onTestRunFinished():void {
        changeCurrentSuite(null);
        traceRaw("Finish");

        // wait a bit to ensure that message has been delivered to the IDE
        setTimeout(function ():void {
            doQuit(null);
        }, 200);
    }

    public function onLogMessage(message:String):void {
        traceRaw(message);
    }

    private function traceCommand(command:String, param1Name:String, param1Value:String, param2Name:String = null, param2Value:String = null, param3Name:String = null, param3Value:String = null):void {
        var line:String = "##teamcity[" + command + " " + param1Name + "='" + escape(param1Value) + "'";
        if (param2Name != null) {
            line += " " + param2Name + "='" + escape(param2Value) + "'";
        }
        if (param3Name != null) {
            line += " " + param3Name + "='" + escape(param3Value) + "'";
        }

        //var date:Date = new Date();
        //var timestamp:String = dateFormatter.format(date) + "." + date.getMilliseconds();
        //line += " timestamp='" + timestamp + "'";
        line += "]";
        traceRaw(line);
    }

    private function doQuit(error:String):void {
        if (_socket.connected) {
            _socket.close();
        }
        doClose(error);
    }

    private function doClose(error:String):void {
        try {
            closeApp();
        } catch (e:Error) {
            // quit failed (i.e. SWF is opened in browser), prompt user to close
            if (error == null) {
                dispatchEvent(new UpdateTextEvent(MAIN_MESSAGE, "FlexUnit test run finished. Please close this window."));
                dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, ""));
            } else {
                dispatchEvent(new UpdateTextEvent(MAIN_MESSAGE, "Failed to run FlexUnit tests. Please close this window."));
                dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, error));
            }
        }
    }

    private static function closeApp():void {
        try {
            var appHolder:Class = getDefinitionByName("flash.desktop.NativeApplication") as Class;
            var appRef:* = appHolder["nativeApplication"];
            if (appRef != null) {
                appRef["exit"](0);
                return;
            }
        } catch (e:ReferenceError) {
            // skip
        }
        fscommand("quit");
    }

    private function changeCurrentSuite(newSuite:String):void {
        if (newSuite == null || newSuite.search(WARNING_TEST_CASE) == -1) {
            var newSuiteName:String = newSuite != null ? getClassName(newSuite) : null;
            if (newSuiteName == _currentSuiteName) {
                return;
            }

            if (_currentSuiteName != null) {
                traceCommand("testSuiteFinished", "name", _currentSuiteName);
            }
            _currentSuiteName = newSuiteName;

            if (_currentSuiteName != null) {
                traceCommand("testSuiteStarted", "name", _currentSuiteName, "locationHint", LOCATION_PROTOCOL + _moduleName + ":" + _currentSuiteName);
            }
        } else {
            if (_currentSuiteName != null) {
                traceCommand("testSuiteFinished", "name", _currentSuiteName);
            }
            _currentSuiteName = null;

            var index:Number = newSuite.search(NO_TESTS_FOUND_WARNING_PREFIX);
            if (index != -1) {
                var className:String = newSuite.substr(index + NO_TESTS_FOUND_WARNING_PREFIX.length).replace("::", ".");
                traceCommand("testSuiteStarted", "name", className, "locationHint", LOCATION_PROTOCOL + _moduleName + ":" + className);
                traceCommand("testStarted", "name", "(no test)");
                traceCommand("testIgnored", "name", "(no test)", "message", "no test found");
                traceCommand("testFinished", "name", "(no test)");
                traceCommand("testSuiteFinished", "name", className);
            }
        }
    }

    private function socketClosed(event:Event):void {
    }

    private function socketConnected(event:Event):void {
        dispatchEvent(new UpdateTextEvent(STATUS_MESSAGE, "Executing tests..."));

        if (_beforeRunTests != null) {
            _beforeRunTests(this);
        }
        runTests();
    }

    private function socketError(event:IOErrorEvent):void {
        connectionFailure(event.text);
    }

    private function socketSecurityError(event:SecurityErrorEvent):void {
        connectionFailure(event.text);
    }

    private function socketData(event:ProgressEvent):void {
        var data:String = _socket.readUTFBytes(_socket.bytesAvailable);
        if ("Finish" == data) {
            doQuit(null);
        }
    }

    private function connectionFailure(text:String):void {
        dispatchEvent(new FailureEvent("Failed to connect to IDEA: " + text, "FlexUnit", function ():void {
            doQuit(text);
        }));
    }

    private function traceRaw(text:String):void {
        if (_socket.connected) {
            _socket.writeUTF(text);
            _socket.flush();
        }
    }

    private static function escape(text:String):String {
        return text.replace(/\|/g, "||")
                .replace(/'/g, "|'")
                .replace(/\n/g, "|n")
                .replace(/\r/g, "|r")
                .replace(/\]/g, "|]")
                .replace(String.fromCharCode(0x2028), "|n")
                .replace(String.fromCharCode(0x2029), "|n");
    }

    private static function getClassName(classAndMethod:String):String {
        var index:Number = classAndMethod.lastIndexOf(".");
        return classAndMethod.substr(0, index).replace("::", ".");
    }

    private static function getMethodName(classAndMethod:String):String {
        var index:Number = classAndMethod.lastIndexOf(".");
        return classAndMethod.substr(index + 1, classAndMethod.length);
    }

}
}
