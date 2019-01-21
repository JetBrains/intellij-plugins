// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2Declaration extends Angular2Entity {

  @Nullable
  default Angular2Module getModule() {
    return ArrayUtil.getFirstElement(getAllModules());
  }

  @NotNull
  Angular2Module[] getAllModules();
}
