package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;

/**
* Created by IntelliJ IDEA.
* User: Maxim
* Date: 24.10.2010
* Time: 7:26:43
* To change this template use File | Settings | File Templates.
*/
public class MaiaFacade extends IdeaFacade {
public Sdk getProjectSdk(ProjectRootManager projectRootManager) {
  return projectRootManager.getProjectJdk();
}
}
