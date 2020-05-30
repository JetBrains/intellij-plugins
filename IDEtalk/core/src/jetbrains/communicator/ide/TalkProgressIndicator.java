// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.ide;

/**
 * @author Kir
 */
public interface TalkProgressIndicator {

  void setIndefinite(boolean indefinite);
  void setText(String text);
  void setFraction(double x);

  void checkCanceled();
}
