package com.intellij.flexunit.launcher {
import com.bit101.components.Component;
import com.bit101.components.PushButton;
import com.bit101.components.Style;
import com.bit101.components.Window;
import com.intellij.flexunit.runner.FailureEvent;
import com.intellij.flexunit.runner.TestRunnerBase;
import com.intellij.flexunit.runner.UpdateTextEvent;

import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;
import flash.text.TextFormatAlign;

[SWF(width=500, height=375, backgroundColor=0)]
public class TestLauncherAs extends Sprite {
    private var _mainText:TextField;

    private var _statusText:TextField;

    public function TestLauncherAs() {
        Component.initStage(stage);

        Style.fontName = "Verdana";
        Style.fontSize = 14;
        Style.embedFonts = false;

        var f:TextFormat = new TextFormat();
        f.align = TextFormatAlign.CENTER;
        f.size = 14;
        f.font = "Verdana";

        _mainText = new TextField();
        _mainText.autoSize = TextFieldAutoSize.CENTER;
        _mainText.defaultTextFormat = f;
        _mainText.y = 10;
        setMainText("Connecting to IntelliJ IDEA...");
        addChild(_mainText);

        _statusText = new TextField();
        _statusText.autoSize = TextFieldAutoSize.CENTER;
        _statusText.text = "dummy";
        _statusText.y = (stage.stageWidth - _mainText.y - _mainText.textHeight - _statusText.textHeight) / 2;
        _statusText.defaultTextFormat = f;
        addChild(_statusText);


        var testRunner:TestRunnerBase = createTestRunner(dataPort, socketPolicyPort, moduleName, null);
        testRunner.addEventListener(TestRunnerBase.MAIN_MESSAGE, function (event:UpdateTextEvent):void {
            setMainText(event.newText);
        });

        testRunner.addEventListener(TestRunnerBase.STATUS_MESSAGE, function (event:UpdateTextEvent):void {
            setStatusText(event.newText);
        });

        var mainPanel:Sprite = this;
        testRunner.addEventListener(FailureEvent.TYPE, function (event:FailureEvent):void {
            var popup:Window = new Window(mainPanel, 0, 0, event.title);
            var messageText:TextField = new TextField();
            messageText.defaultTextFormat = f;
            messageText.width = 300;
            messageText.text = event.message;
            messageText.wordWrap = true;
            popup.content.addChild(messageText);
            popup.setSize(messageText.width + 20, messageText.height + 20);
            popup.x = (stage.stageWidth - popup.width) / 2;
            popup.y = (stage.stageHeight - popup.height) / 2;

            var pushButton:PushButton = new PushButton(popup.content);
            pushButton.label = "OK";
            pushButton.addEventListener(MouseEvent.CLICK, event.callback);
            pushButton.x = (popup.content.width - pushButton.width) / 2;
            pushButton.y = popup.content.height - pushButton.height - 5;
        });

        addTests(testRunner);
        testRunner.run();
    }

    private function setStatusText(text:String):void {
        _statusText.text = text;
        _statusText.x = (stage.stageWidth - _statusText.textWidth) / 2;
    }

    private function setMainText(text:String):void {
        _mainText.text = text;
        _mainText.x = (stage.stageWidth - _mainText.textWidth) / 2;
    }

    protected function createTestRunner(port:int, socketPolicyPort:int, moduleName:String, beforeRunTests:Function):TestRunnerBase {
        throw new Error("Not implemented");
    }

    protected function get dataPort():int {
        throw new Error("Not implemented");
    }

    protected function get socketPolicyPort():int {
        throw new Error("Not implemented");
    }

    protected function get moduleName():String {
        throw new Error("Not implemented");
    }

    protected function addTests(__testRunner:TestRunnerBase):void {
        throw new Error("Not implemented");
    }
}
}
