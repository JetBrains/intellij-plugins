// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import org.jetbrains.annotations.Nullable;

public interface Angular2Declaration extends Angular2Entity {

  @Nullable
  Angular2Module getModule();
}
