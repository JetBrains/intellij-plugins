// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.ide;

/**
 * @author Kir
 */
public class NullProgressIndicator implements TalkProgressIndicator {
  @Override
  public void setIndefinite(boolean indefinite) {
  }

  @Override
  public void setText(String text) {
  }

  @Override
  public void setFraction(double x) {
  }

  @Override
  public void checkCanceled() {
  }
}
