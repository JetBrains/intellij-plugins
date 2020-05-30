// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core;

/**
 * @author Kir Maximov
 */
public interface IDEtalkListener {
  void beforeChange(IDEtalkEvent event);

  void afterChange(IDEtalkEvent event);
}
