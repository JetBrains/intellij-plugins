/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
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
package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.ImplicitRequireProvider;
import org.jetbrains.plugins.ruby.utils.RubyVirtualFileScanner;
import org.jetbrains.plugins.ruby.utils.VirtualFileUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import static com.intellij.util.containers.ContainerUtil.addIfNotNull;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionRequireProvider implements ImplicitRequireProvider {
  @NotNull
  @Override
  public Collection<VirtualFile> getLoadPath(@Nullable Module module, PsiFile file) {
    if (module == null) return Collections.emptySet();
    if (!RubyMotionUtil.getInstance().hasMacRubySupport(file)) return Collections.emptySet();

    final LinkedHashSet<VirtualFile> result = new LinkedHashSet<>();
    final VirtualFile motion = VirtualFileUtil.findFileBy(RubyMotionUtil.getInstance().getRubyMotionPath() + "/lib");
    addIfNotNull(result, motion);
    for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
      addIfNotNull(result, root.findChild("app"));
    }
    return result;
  }

  @NotNull
  @Override
  public Collection<VirtualFile> getImplicitRequires(@NotNull Module module, PsiFile file) {
    if (!RubyMotionUtil.getInstance().hasMacRubySupport(file)) return Collections.emptySet();
    final VirtualFile vFile = file.getVirtualFile();
    final boolean isTestFile = vFile != null && vFile.getPath().contains("/spec/");
    final LinkedHashSet<VirtualFile> result = new LinkedHashSet<>();
    final Collection<VirtualFile> loadPath = getLoadPath(module, file);
    for (VirtualFile path : loadPath) {
      if (path.getPath().startsWith(RubyMotionUtil.getInstance().getRubyMotionPath())) {
        if (isTestFile) {
          addIfNotNull(result, path.findFileByRelativePath("motion/spec.rb"));
        }
        continue;
      }
      RubyVirtualFileScanner.addRubyFiles(path, result);
    }
    return result;
  }
}
