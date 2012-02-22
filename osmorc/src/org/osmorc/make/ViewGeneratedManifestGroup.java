/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.make;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Action group, which holds references to the jar files that are created by
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class ViewGeneratedManifestGroup extends ActionGroup {
  private static final AnAction[] EMPTY_ACTIONS_LIST = new AnAction[0];

  @Override
  public void update(AnActionEvent e) {
    // IDEA-79063 only show this when there are actually osmorc facets available.
    boolean enabled = false;
    Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project != null) {
      for (Module m : ModuleManager.getInstance(project).getModules()) {
        if (OsmorcFacet.hasOsmorcFacet(m)) {
          enabled = true;
          break;
        }
      }
    }
    e.getPresentation().setVisible(enabled);
  }

  @NotNull
  @Override
  public AnAction[] getChildren(@Nullable AnActionEvent e) {
    if (e == null) return EMPTY_ACTIONS_LIST;
    @Nullable Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) {
      return EMPTY_ACTIONS_LIST;
    }

    final List<AnAction> actions = new ArrayList<AnAction>();
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (OsmorcFacet.hasOsmorcFacet(module)) {
        OsmorcFacet facet = OsmorcFacet.getInstance(module);
        if (facet == null) continue;
        String jarFile = facet.getConfiguration().getJarFileLocation();
        File theFile = new File(jarFile);
//                if (theFile.exists()) {
        String fileName = "[" + module.getName() + "] " + jarFile.substring(jarFile.lastIndexOf('/') + 1);
        ViewManifestAction action = new ViewManifestAction(fileName, jarFile);
        actions.add(action);
//                }
      }
    }
    return actions.toArray(new AnAction[actions.size()]);
  }
}
