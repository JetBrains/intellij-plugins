// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.Icon;

/**
 * File type for Bnd/Bndtools files.
 */
public class BndFileType extends LanguageFileType {
  public static final String BND_EXT = "bnd";
  public static final String BND_RUN_EXT = "bndrun";

  public static final BndFileType INSTANCE = new BndFileType();

  private BndFileType() {
    super(BndLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "bnd";
  }

  @Override
  public @NotNull String getDescription() {
    return OsmorcBundle.message("bnd.file.type");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return BND_EXT;
  }

  @Override
  public @Nullable Icon getIcon() {
    return AllIcons.FileTypes.Config;
  }
}
