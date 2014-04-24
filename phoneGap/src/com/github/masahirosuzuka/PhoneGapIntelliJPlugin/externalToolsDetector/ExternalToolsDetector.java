package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.externalToolsDetector;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Masahiro Suzuka on 2014/04/16.
 */
public class ExternalToolsDetector implements ProjectComponent {

  private static Project project;

  public ExternalToolsDetector(Project project) {
    this.project = project;
  }

  public void initComponent() {
    // Detect node.js
    NodeJSDetectorThread nodeJSDetectorThread = new NodeJSDetectorThread(project);
    nodeJSDetectorThread.run();

    // Detect phonegap cli
    PhoneGapDetectThread phoneGapDetectThread = new PhoneGapDetectThread(project);
    phoneGapDetectThread.run();

    // Detect AndroidSDK
    AndroidSDKDetectorThread androidSDKDetectorThread = new AndroidSDKDetectorThread(project);
    androidSDKDetectorThread.run();

    // Detect iOS SDK & Detect ios-sim
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
