// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angularjs.cli;

import com.google.gson.JsonParseException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.JsonReaderEx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class AngularCliJsonInfo {
  private static final Key<AngularCliJsonInfo> ANGULAR_CLI_JSON_INFO_KEY = Key.create("ANGULAR_CLI_JSON_INFO_KEY");

  private final long myModStamp;
  @NotNull private final Collection<String> myRootPaths;
  @NotNull private final Collection<String> myStylePreprocessorIncludePaths;

  public AngularCliJsonInfo(final long modStamp,
                            @NotNull final Collection<String> rootPaths,
                            @NotNull final Collection<String> stylePreprocessorIncludePaths) {
    myModStamp = modStamp;
    myRootPaths = rootPaths;
    myStylePreprocessorIncludePaths = stylePreprocessorIncludePaths;
  }

  /**
   * @return root folders according to apps -> root in .angular-cli.json; usually it is a single 'src' folder.
   */
  @NotNull
  public static Collection<VirtualFile> getRootDirs(@NotNull final Project project, @NotNull final VirtualFile context) {
    final VirtualFile angularCliFolder = AngularCliUtil.findAngularCliFolder(project, context);
    final VirtualFile angularCliJson = angularCliFolder == null ? null : AngularCliUtil.findCliJson(angularCliFolder);
    if (angularCliJson == null) {
      return Collections.emptyList();
    }

    final AngularCliJsonInfo info = getAngularCliJsonInfo(angularCliJson);
    return ContainerUtil.mapNotNull(info.myRootPaths, s -> angularCliFolder.findFileByRelativePath(s));
  }

  /**
   * @return folders that are precessed as root folders by style preprocessor according to apps -> stylePreprocessorOptions -> includePaths in .angular-cli.json
   */
  @NotNull
  public static Collection<VirtualFile> getStylePreprocessorIncludeDirs(@NotNull final Project project,
                                                                        @NotNull final VirtualFile context) {
    final VirtualFile angularCliFolder = AngularCliUtil.findAngularCliFolder(project, context);
    final VirtualFile angularCliJson = angularCliFolder == null ? null : AngularCliUtil.findCliJson(angularCliFolder);
    if (angularCliJson == null) {
      return Collections.emptyList();
    }

    final AngularCliJsonInfo info = getAngularCliJsonInfo(angularCliJson);
    final Collection<VirtualFile> result = new ArrayList<>(info.myRootPaths.size() * info.myStylePreprocessorIncludePaths.size());
    for (String rootPath : info.myRootPaths) {
      for (String includePath : info.myStylePreprocessorIncludePaths) {
        ContainerUtil.addIfNotNull(result, angularCliFolder.findFileByRelativePath(rootPath + "/" + includePath));
      }
    }
    return result;
  }

  @NotNull
  private static AngularCliJsonInfo getAngularCliJsonInfo(@NotNull final VirtualFile angularCliJson) {
    final Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(angularCliJson);
    final long modStamp = cachedDocument != null ? cachedDocument.getModificationStamp() : angularCliJson.getModificationCount();
    final AngularCliJsonInfo info = angularCliJson.getUserData(ANGULAR_CLI_JSON_INFO_KEY);
    if (info != null && info.myModStamp == modStamp) {
      return info;
    }
    try {
      final CharSequence text = cachedDocument != null ? cachedDocument.getCharsSequence() : VfsUtilCore.loadText(angularCliJson);
      final AngularCliJsonInfo newInfo = parseInfo(text, modStamp);
      angularCliJson.putUserData(ANGULAR_CLI_JSON_INFO_KEY, newInfo);
      return newInfo;
    }
    catch (IOException e) {
      return new AngularCliJsonInfo(-1, Collections.emptyList(), Collections.emptyList());
    }
  }

  @NotNull
  private static AngularCliJsonInfo parseInfo(@NotNull final CharSequence text, final long modStamp) {
    final Collection<String> rootPaths = new SmartList<>();
    final Collection<String> includePaths = new SmartList<>();
    try (JsonReaderEx reader = new JsonReaderEx(text)) {
      reader.beginObject();
      while (reader.hasNext()) {
        final String topLevelName = reader.nextName();
        if ("apps".equals(topLevelName)) {
          reader.beginArray();
          while (reader.hasNext()) {
            readProject(reader, rootPaths, includePaths);
          }
          reader.endArray();
          break;
        }
        else if ("projects".equals(topLevelName)) {
          reader.beginObject();
          while (reader.hasNext()) {
            reader.nextName();
            readProject(reader, rootPaths, includePaths);
          }
          reader.endObject();
        }
        else {
          reader.skipValue();
        }
      }
    }
    catch (IllegalStateException | JsonParseException ignore) {/* unlucky */}

    return new AngularCliJsonInfo(modStamp, rootPaths, includePaths);
  }

  private static void readProject(JsonReaderEx reader, Collection<String> rootPaths, Collection<String> includePaths) {
    reader.beginObject();
    while (reader.hasNext()) {
      final String nextName = reader.nextName();
      if ("root".equals(nextName)) {
        rootPaths.add(reader.nextString());
      }
      else if ("stylePreprocessorOptions".equals(nextName)) {
        readIncludePaths(reader, includePaths);
      }
      else if ("architect".equals(nextName)) {
        readBuildOptions(reader, includePaths);
      }
      else {
        reader.skipValue();
      }
    }
    reader.endObject();
  }

  private static void readIncludePaths(@NotNull final JsonReaderEx reader, @NotNull final Collection<? super String> includePaths) {
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        if ("includePaths".equals(reader.nextName())) {
          reader.beginArray();
          while (reader.hasNext()) {
            includePaths.add(reader.nextString());
          }
          reader.endArray();
        }
        else {
          reader.skipValue();
        }
      }
      reader.endObject();
    }
    catch (IllegalStateException | JsonParseException ignore) {
      reader.skipValue();
    }
  }

  private static void readBuildOptions(@NotNull final JsonReaderEx reader, @NotNull final Collection<? super String> includePaths) {
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        if ("build".equals(reader.nextName())) {
          reader.beginObject();
          while (reader.hasNext()) {
            if ("options".equals(reader.nextName())) {
              reader.beginObject();
              while (reader.hasNext()) {
                if ("stylePreprocessorOptions".equals(reader.nextName())) {
                  readIncludePaths(reader, includePaths);
                }
                else {
                  reader.skipValue();
                }
              }
              reader.endObject();
            }
            else {
              reader.skipValue();
            }
          }
          reader.endObject();
        }
        else {
          reader.skipValue();
        }
      }
      reader.endObject();
    }
    catch (IllegalStateException | JsonParseException ignore) {
      reader.skipValue();
    }
  }
}
