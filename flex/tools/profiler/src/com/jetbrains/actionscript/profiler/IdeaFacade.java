package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 23.10.10
 * Time: 14:23
 */
public abstract class IdeaFacade {
  private static final Logger LOG = Logger.getInstance(IdeaFacade.class.getName());
  private static IdeaFacade instance;

  public static IdeaFacade getInstance() {
    if (instance == null) {
      ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
      String className;
      if (applicationInfo.getMajorVersion().equals("9")) {
        className = "MaiaFacade";
      } else {
        className = "IdeaXFacade";
      }

      try {
        instance = (IdeaFacade) Class.forName("com.jetbrains.actionscript.profiler."+className).newInstance();
      } catch (Exception e) {
        LOG.warn(e);
      }
    }
    return instance;
  }

  public abstract Sdk getProjectSdk(ProjectRootManager projectRootManager);

}
