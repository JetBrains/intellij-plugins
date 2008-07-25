/*
 * Copyright 2007 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.facet.ui;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Groups one or more struts.xml files in a named set.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileSet implements ElementsChooser.ElementProperties {

  private final String id;
  private String name;

  private boolean autodetected;
  private boolean removed;

  private final List<VirtualFilePointer> files = new ArrayList<VirtualFilePointer>();

  @NonNls private static final String ID_PREFIX = "s2fileset";

  public StrutsFileSet(@NotNull final String id, @NotNull final String name) {
    this.id = id;
    this.name = name;
  }

  public StrutsFileSet(@NotNull final StrutsFileSet original) {
    this(original.getId(), original.getName());
    autodetected = original.isAutodetected();
    removed = original.isRemoved();
    files.addAll(original.getFiles());
  }

  public static String getUniqueId(final Set<StrutsFileSet> list) {
    int index = 0;
    for (final StrutsFileSet fileSet : list) {
      if (fileSet.getId().startsWith(ID_PREFIX)) {
        final String s = fileSet.getId().substring(ID_PREFIX.length());
        try {
          final int i = Integer.parseInt(s);
          index = Math.max(i, index);
        } catch (NumberFormatException e) {
          //
        }
      }
    }
    return ID_PREFIX + (index + 1);
  }

  public static String getUniqueName(final String prefix, final Set<StrutsFileSet> list) {
    int index = 0;
    for (final StrutsFileSet fileSet : list) {
      if (fileSet.getName().startsWith(prefix)) {
        final String s = fileSet.getName().substring(prefix.length());
        int i;
        try {
          i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
          i = 0;
        }
        index = Math.max(i + 1, index);
      }
    }
    return index == 0 ? prefix : prefix + " " + index;
  }

  public Icon getIcon() {
    return Icons.PACKAGE_ICON;
  }

  public Color getColor() {
    return null;
  }

  public boolean isNew() {
    return false;
  }

  @NotNull
  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(@NotNull final String name) {
    this.name = name;
  }

  public boolean isAutodetected() {
    return autodetected;
  }

  public void setAutodetected(final boolean autodetected) {
    this.autodetected = autodetected;
  }

  public boolean isRemoved() {
    return removed;
  }

  public void setRemoved(final boolean removed) {
    this.removed = removed;
  }

  @NotNull
  public List<VirtualFilePointer> getFiles() {
    return files;
  }

  public void addFile(final VirtualFile file) {
    addFile(file.getUrl());
  }

  public void addFile(@NonNls final String url) {
    final VirtualFilePointer filePointer = VirtualFilePointerManager.getInstance().create(url, null);
    files.add(filePointer);
  }

  public void removeFile(final VirtualFilePointer file) {
    files.remove(file);
  }

  public boolean hasFile(@Nullable final VirtualFile file) {
    if (file == null) {
      return false;
    }
    for (final VirtualFilePointer pointer : files) {
      final VirtualFile virtualFile = pointer.getFile();
      if (virtualFile != null && file.equals(virtualFile)) {
        return true;
      }
    }
    return false;
  }

  public boolean equals(final Object another) {
    if (another instanceof StrutsFileSet) {
      final StrutsFileSet obj = (StrutsFileSet) another;
      return obj.getId().equals(id);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return id.hashCode();
  }

  public String toString() {
    return name;
  }
}
