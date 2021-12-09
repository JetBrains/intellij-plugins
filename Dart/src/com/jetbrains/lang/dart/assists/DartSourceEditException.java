// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.assists;

import org.jetbrains.annotations.Nls;

public class DartSourceEditException extends Exception {
  public DartSourceEditException(@Nls String message) {
    super(message);
  }
}
