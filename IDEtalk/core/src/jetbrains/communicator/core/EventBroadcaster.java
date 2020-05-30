// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core;

import org.jetbrains.annotations.NotNull;

/**
 * @author Kir
 */
public interface EventBroadcaster {

  void doChange(@NotNull IDEtalkEvent event, Runnable action);
  void fireEvent(@NotNull IDEtalkEvent event);

  void addListener(IDEtalkListener listener);
  void removeListener(IDEtalkListener listener);
}
