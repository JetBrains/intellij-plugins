package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.projectWizard.Stagehand.StagehandTuple;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartTemplatesFactory extends ProjectTemplatesFactory {

  private static final String GROUP_NAME = "Dart";

  private static final Stagehand myStagehand = new Stagehand();
  private static List<DartEmptyProjectGenerator> myTemplateCache;

  @NotNull
  @Override
  public String[] getGroups() {
    return new String[]{GROUP_NAME};
  }

  @Override
  public String getParentGroup(String group) {
    return WebModuleBuilder.GROUP_NAME;
  }

  @NotNull
  @Override
  public ProjectTemplate[] createTemplates(String group, WizardContext context) {

    final List<DartEmptyProjectGenerator> templates = getStagehandTemplates();

    if (!templates.isEmpty()) {
      return templates.toArray(StagehandTemplateGenerator.EMPTY_ARRAY);
    }

    // Fall back
    return new ProjectTemplate[]{
      new DartEmptyProjectGenerator(DartBundle.message("empty.dart.project.description.idea")),
      new DartWebAppGenerator(DartBundle.message("dart.web.app.description.idea")),
      new DartCmdLineAppGenerator(DartBundle.message("dart.commandline.app.description.idea"))
    };
  }

  private static List<DartEmptyProjectGenerator> getStagehandTemplates() {

    if (myTemplateCache != null) {
      return myTemplateCache;
    }

    boolean doUpgradeCheck = true;

    if (!myStagehand.isInstalled()) {
      doUpgradeCheck = false;
      myStagehand.install();
    }

    List<StagehandTuple> templates = myStagehand.getAvailableTemplates();

    // Make sure we're on a reasonably latest version of Stagehand.
    if (doUpgradeCheck) {
      new Thread() {
        @Override
        public void run() {
          myStagehand.upgrade();
        }
      }.start();
    }

    myTemplateCache = new ArrayList<DartEmptyProjectGenerator>();

    for (StagehandTuple template : templates) {
      myTemplateCache.add(new StagehandTemplateGenerator(
        myStagehand,
        template.myId,
        template.myDescription,
        template.myEntrypoint));
    }

    Collections.sort(myTemplateCache);

    return myTemplateCache;
  }


  @Override
  public Icon getGroupIcon(final String group) {
    return DartIcons.Dart_16;
  }
}
