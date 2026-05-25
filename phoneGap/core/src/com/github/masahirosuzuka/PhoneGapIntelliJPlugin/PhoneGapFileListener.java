// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.PhoneGapSettings;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapStartupActivity.getExcludedFolderNames;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapStartupActivity.getUpdateModuleExcludeByFSEventRunnable;

public final class PhoneGapFileListener implements AsyncFileListener {

  @Override
  public @Nullable ChangeApplier prepareChange(@NotNull List<? extends @NotNull VFileEvent> events) {
    List<Pair<VFileEvent, VirtualFile>> acceptableEvents = null;
    
    for (VFileEvent event : events) {
      Pair<VFileEvent, VirtualFile> toAdd = null;
      if (event instanceof VFileCreateEvent) {
        ProgressManager.checkCanceled();
        
        if (!isProcess(event)) continue;
        VirtualFile parent = PhoneGapStartupActivity.getEventParent(event);
        if (parent == null) continue;
        toAdd = Pair.create(event, parent);
      }
      else if (event instanceof VFileDeleteEvent) {
        if (!isProcess(event)) continue;
        VirtualFile parent = PhoneGapStartupActivity.getEventParent(event);
        if (parent == null) continue;
        toAdd = Pair.create(event, parent);
      }
      if (toAdd != null) {
        if (acceptableEvents == null) acceptableEvents = new SmartList<>();
        acceptableEvents.add(toAdd);
      }
    }
    List<Pair<VFileEvent, VirtualFile>> finalEvents = acceptableEvents;
    return finalEvents == null ? null : new ChangeApplier() {
      @Override
      public void afterVfsChange() {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
          ProjectFileIndex index = ProjectFileIndex.getInstance(project);
          for (Pair<VFileEvent, VirtualFile> eventWithFile : finalEvents) {
            VFileEvent event = eventWithFile.first;
            VirtualFile file = eventWithFile.second;
            if (file == null || !file.isValid() || !index.isInContent(file)) continue;
            Runnable runnable = event instanceof VFileCreateEvent ?
            getUpdateModuleExcludeByFSEventRunnable(project, file, Collections.emptySet(), getExcludedFolderNames(event)) : 
            getUpdateModuleExcludeByFSEventRunnable(project, file, getExcludedFolderNames(event), Collections.emptySet());
            if (runnable != null) {
              runnable.run();
            }
          }  
        }
        
      }
    };
  }
  

  private static boolean isProcess(@NotNull VFileEvent event) {
    return PhoneGapSettings.getInstance().isExcludePlatformFolder() &&
           PhoneGapStartupActivity.shouldExcludeDirectory(event);
  }
}
