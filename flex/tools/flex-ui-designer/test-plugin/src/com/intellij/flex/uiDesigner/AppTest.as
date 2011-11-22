package com.intellij.flex.uiDesigner {
import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.events.Event;

import org.hamcrest.assertThat;
import org.hamcrest.collection.arrayWithSize;
import org.hamcrest.object.nullValue;

public class AppTest extends BaseTestCase {
  public function AppTest() {
    // disable unused inspection
    //noinspection ConstantIfStatementJS
    if (false) {
      close();
    }
  }
  
  [Test(async, nullableDocument)]
  public function close():void {
    assertThat(project, nullValue());
    
    var openedWindows:Array = NativeApplication.nativeApplication.openedWindows;
    // it can not have time to be closed
    if (openedWindows.length == 1) {
      assertThat(NativeApplication.nativeApplication.activeWindow, nullValue());
      _asyncSuccessHandler();
    }
    else {
      assertThat(openedWindows, arrayWithSize(2));
      NativeWindow(openedWindows[1]).addEventListener(Event.CLOSE, function (event:Event):void {
      assertThat(NativeApplication.nativeApplication.activeWindow, nullValue());
      assertThat(NativeApplication.nativeApplication.openedWindows, arrayWithSize(1));

      asyncSuccess(event, arguments.callee);
    });
    }
  }
  
  public function wait():void {
  }

  [Test(document="SetProperty")]
  public function UpdateDocumentOnIdeaAutoSave():void {
    assertThat(app.getElementAt(0), l("mmA"));
  }
}
}