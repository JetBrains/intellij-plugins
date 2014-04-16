package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Masahiro Suzuka on 2014/04/16.
 */
public class PhoneGapDetector implements ProjectComponent {

  private static Project project;

  public PhoneGapDetector(Project project) {
    this.project = project;
  }

  public void initComponent() {
    // Detect phonegap cli
    PhoneGapDetectThread phoneGapDetectThread = new PhoneGapDetectThread(project);
    phoneGapDetectThread.run();

    // Detect AndroidSDK

    // Detect iOS SDK & Detect ios-sim
  }

  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  @NotNull
  public String getComponentName() {
    return "PhoneGapDetector";
  }

  public void projectOpened() {
    // called when project is opened
  }

  public void projectClosed() {
    // called when project is being closed
  }
}
