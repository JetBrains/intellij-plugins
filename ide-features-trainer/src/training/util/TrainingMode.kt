/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.util

enum class TrainingMode {
  NORMAL {
    override val doesShowResetButton = false
  },
  DEMO {
    override val doesShowResetButton = true
  },
  DEVELOPMENT {
    override val doesShowResetButton = true
  };

  abstract val doesShowResetButton: Boolean
}