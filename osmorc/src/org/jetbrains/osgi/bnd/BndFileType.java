/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.osgi.bnd;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * File type for bnd (osgi bundle) files.
 *
 * @author <a href="mailto:tibor@malanik.eu">Tibor Malanik</a>
 */
public class BndFileType extends LanguageFileType {

  public static final String BND_EXT = "bnd";
  public static final String BND_RUN_EXT = "bndrun";

  public static BndFileType INSTANCE = new BndFileType();

  private BndFileType() {
    super(BndLanguage.INSTANCE);
  }

  @NotNull
  @NonNls
  public String getName() {
    return "bnd";
  }

  @NotNull
  public String getDescription() {
    return "bnd";
  }

  @NotNull
  @NonNls
  public String getDefaultExtension() {
    return BND_EXT;
  }

  @Nullable
  public Icon getIcon() {
    return AllIcons.FileTypes.Config;
  }

}
