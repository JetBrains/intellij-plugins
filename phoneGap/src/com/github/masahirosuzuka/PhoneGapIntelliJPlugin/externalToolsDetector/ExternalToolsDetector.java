package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.execution.Platform;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

/**
 * ExternalToolsDetector.java
 *
 * Created by Masahiro Suzuka on 2014/04/16.
 */

//
// I have found that these are not suitable for culture of IntelliJ.
// But I leave for absolutely necessary only 'node.js detector' and 'phonegap detector'.
//

public class ExternalToolsDetector implements ProjectComponent {

  private static Project project;

  public ExternalToolsDetector(Project project) {
    this.project = project;
  }

  public void initComponent() {
    // Detect node.js
    if (SystemInfo.isWindows) {
      if (SystemInfo.is32Bit) { // System is Windows32bit
        // Program Files(x86)
        PhoneGapSettings.getInstance().PHONEGAP_PATH = "";
      }

      if (SystemInfo.is64Bit) { // System is Windows64bit
        // Program Files
      }
    } else { // System is Mac or Linux
      NodeJSDetectorThread nodeJSDetectorThread = new NodeJSDetectorThread(project);
      nodeJSDetectorThread.run();

      // Detect phonegap cli
      PhoneGapDetectThread phoneGapDetectThread = new PhoneGapDetectThread(project);
      phoneGapDetectThread.run();

      // if installed intellij is AndroidStudio use AndroidStudio's Android dev-tools

      // Detect AndroidSDK
      //AndroidSDKDetectorThread androidSDKDetectorThread = new AndroidSDKDetectorThread(project);
      //androidSDKDetectorThread.run();

      // Detect iOS SDK & Detect ios-sim
      // Only Mac
      //if (SystemInfo.isMac) {
      //  iOSSDKdetectorThread iosSDKdetectorThread = new iOSSDKdetectorThread(project);
      //  iosSDKdetectorThread.run();
      //}
    }
  }

  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  @NotNull
  public String getComponentName() {
    return "ExternalToolsDetector";
  }

  public void projectOpened() {
    // called when project is opened
  }

  public void projectClosed() {
    // called when project is being closed
  }
}
