// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlFileNSInfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VueNSInfoProvider implements XmlFileNSInfoProvider {
  @Override
  public String[] @Nullable [] getDefaultNamespaces(@NotNull XmlFile file) {
    return null;
  }

  @Override
  public boolean overrideNamespaceFromDocType(@NotNull XmlFile file) {
    return file.getFileType() == VueFileType.INSTANCE;
  }
}
