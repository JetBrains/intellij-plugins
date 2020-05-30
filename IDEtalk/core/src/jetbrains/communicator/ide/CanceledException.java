// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.ide;

/**
 * @author Kir
 */
public class CanceledException extends Exception {
  public CanceledException() {
  }

  public CanceledException(Throwable cause) {
    super(cause);
  }
}
