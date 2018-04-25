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
import java.util.Collection;
import java.util.Collections;

public class AngularCliJsonInfo {
  private static final Key<AngularCliJsonInfo> ANGULAR_CLI_JSON_INFO_KEY = Key.create("ANGULAR_CLI_JSON_INFO_KEY");

  private final long myModStamp;
  @NotNull private final Collection<String> myRootPaths;

  public AngularCliJsonInfo(final long modStamp, @NotNull final Collection<String> rootPaths) {
    myModStamp = modStamp;
    myRootPaths = rootPaths;
  }

  /**
   * @return root folders according to apps -> root in .angular-cli.json; usually it is a single 'src' folder.
   */
  @NotNull
  public static Collection<VirtualFile> getRootDirs(@NotNull final Project project, @NotNull final VirtualFile context) {
    final VirtualFile angularCliFolder = BlueprintsLoaderKt.findAngularCliFolder(project, context);
    final VirtualFile angularCliJson = angularCliFolder == null ? null : AngularJSProjectConfigurator.findCliJson(angularCliFolder);
    if (angularCliJson == null) {
      return Collections.emptyList();
    }

    final AngularCliJsonInfo info = getAngularCliJsonInfo(angularCliJson);
    return ContainerUtil.mapNotNull(info.myRootPaths, s -> angularCliFolder.findFileByRelativePath(s));
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
      return new AngularCliJsonInfo(-1, Collections.emptyList());
    }
  }

  @NotNull
  private static AngularCliJsonInfo parseInfo(@NotNull final CharSequence text, final long modStamp) {
    final Collection<String> result = new SmartList<>();
    try (JsonReaderEx reader = new JsonReaderEx(text)) {
      reader.beginObject();
      while (reader.hasNext()) {
        if ("apps".equals(reader.nextName())) {
          reader.beginArray();
          while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
              if ("root".equals(reader.nextName())) {
                result.add(reader.nextString());
              }
              else {
                reader.skipValue();
              }
            }
            reader.endObject();
          }
          break;
        }
        else {
          reader.skipValue();
        }
      }
    }
    catch (IllegalStateException | JsonParseException ignore) {/* unlucky */}

    return new AngularCliJsonInfo(modStamp, result);
  }
}
