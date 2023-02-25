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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.UniqueNameGenerator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Groups one or more {@code struts.xml} files in a named set.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileSet implements Disposable {

  private final String id;
  private String name;

  private boolean autodetected;
  private boolean removed;

  private final List<VirtualFilePointer> files = new SmartList<>();

  @NonNls
  private static final String ID_PREFIX = "s2fileset";

  private static final Function<StrutsFileSet, String> FILESET_NAME_FUNCTION = strutsFileSet -> strutsFileSet.getName();

  private static final Function<StrutsFileSet, String> FILESET_ID_FUNCTION = strutsFileSet -> strutsFileSet.getId();

  public StrutsFileSet(@NotNull @NonNls final String id,
                       @NotNull @NonNls final String name,
                       @NotNull final StrutsFacetConfiguration parent) {
    this.id = id;
    this.name = name;

    Disposer.register(parent, this);
  }

  public StrutsFileSet(@NotNull final StrutsFileSet original) {
    id = original.id;
    name = original.name;
    files.addAll(original.files);
    autodetected = original.autodetected;
    removed = original.removed;
  }

  public static String getUniqueId(final Set<? extends StrutsFileSet> list) {
    return UniqueNameGenerator.generateUniqueName(ID_PREFIX, ContainerUtil.map(list, FILESET_ID_FUNCTION));
  }

  public static String getUniqueName(final String prefix, final Set<? extends StrutsFileSet> list) {
    return UniqueNameGenerator.generateUniqueName(prefix + " ", ContainerUtil.map(list, FILESET_NAME_FUNCTION));
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
    if (!StringUtil.isEmptyOrSpaces(url)) {
      final VirtualFilePointer filePointer = VirtualFilePointerManager.getInstance().create(url, this, null);
      files.add(filePointer);
    }
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
    if (another instanceof StrutsFileSet obj) {
      return Objects.equals(obj.getId(), id);
    }

    return false;
  }

  public int hashCode() {
    return id.hashCode();
  }

  public String toString() {
    return name;
  }

  @Override
  public void dispose() {
  }

}
