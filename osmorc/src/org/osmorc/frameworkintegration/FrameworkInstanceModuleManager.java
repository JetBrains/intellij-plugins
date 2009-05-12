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

package org.osmorc.frameworkintegration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.osmorc.settings.ProjectSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FrameworkInstanceModuleManager
{
  public FrameworkInstanceModuleManager(LibraryHandler libraryHandler, ProjectSettings projectSettings,
                                        Application application, Project project,
                                        ModuleManager moduleManager)
  {
    _projectSettings = projectSettings;
    _application = application;
    _project = project;
    _libraryHandler = libraryHandler;
    _moduleManager = moduleManager;
  }

  public void updateFrameworkInstanceModule()
  {
    if (_projectSettings.isCreateFrameworkInstanceModule())
    {
      ensureFrameworkInstanceModuleExists();
      updateModuleLibraries(_projectSettings.getFrameworkInstanceName());
    }
    else
    {
      final Module module = getFrameworkInstanceModule();
      if (module != null)
      {
        _application.runWriteAction(new Runnable()
        {
          public void run()
          {
            try
            {
              final VirtualFile file = module.getModuleFile();
              final ModifiableModuleModel moduleModel = _moduleManager.getModifiableModel();
              moduleModel.disposeModule(module);
              moduleModel.commit();
              if (file != null && file.exists())
              {
                file.delete(this);
              }
            }
            catch (IOException e)
            {
              throw new RuntimeException(e);
            }
          }
        });
      }
    }
  }

  private void updateModuleLibraries(@Nullable String instanceName)
  {
    final List<Library> libraries = instanceName != null ? _libraryHandler.getLibraries(instanceName) : new ArrayList<Library>();
    _application.runWriteAction(new Runnable()
    {
      public void run()
      {
        final Module module = getFrameworkInstanceModule();
        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();

        List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
        for (OrderEntry oldOrderEntry : model.getOrderEntries())
        {
          if (oldOrderEntry instanceof LibraryOrderEntry)
          {
            oldOrderEntries.add(oldOrderEntry);
          }
        }

        for (Iterator<OrderEntry> oldOrderEntriesIterator = oldOrderEntries.iterator();
             oldOrderEntriesIterator.hasNext();)
        {
          OrderEntry orderEntry = oldOrderEntriesIterator.next();

          if (orderEntry instanceof LibraryOrderEntry)
          {
            if (libraries.remove(((LibraryOrderEntry) orderEntry).getLibrary()))
            {
              oldOrderEntriesIterator.remove();
            }
          }
        }

        boolean commitNeeded = false;
        for (OrderEntry orderEntry : oldOrderEntries)
        {
          model.removeOrderEntry(orderEntry);
          commitNeeded = true;
        }

        for (Library newBundle : libraries)
        {
          model.addLibraryEntry(newBundle);
          commitNeeded = true;
        }
        if (commitNeeded)
        {
          model.commit();
        }
        else
        {
          model.dispose();
        }
      }
    });
  }

  private void ensureFrameworkInstanceModuleExists()
  {
    final Module frameworkInstanceModule = getFrameworkInstanceModule();
    if (frameworkInstanceModule == null)
    {
      _application.runWriteAction(new Runnable()
      {
        public void run()
        {
          final VirtualFile baseDir = _project.getBaseDir();
          assert baseDir != null;
          String path = baseDir.getPath();
          if (!path.endsWith("/"))
          {
            path = path + '/';
          }
          path = path + FRAMEWORK_INSTANCE_MODULE_NAME + ".iml";
          final Module module = _moduleManager.newModule(path, StdModuleTypes.JAVA);
          ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
          model.setSdk(ProjectRootManager.getInstance(_project).getProjectJdk());
          model.commit();
        }
      });
    }
  }

  private Module getFrameworkInstanceModule()
  {
    return _moduleManager.findModuleByName(FRAMEWORK_INSTANCE_MODULE_NAME);
  }

  private final ProjectSettings _projectSettings;
  private final Application _application;
  private final Project _project;
  private final LibraryHandler _libraryHandler;
  private final ModuleManager _moduleManager;
  protected static final String FRAMEWORK_INSTANCE_MODULE_NAME = "FrameworkInstance";
}
