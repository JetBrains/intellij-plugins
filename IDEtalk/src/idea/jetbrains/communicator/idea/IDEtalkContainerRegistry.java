// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import jetbrains.communicator.core.Pico;
import org.picocontainer.MutablePicoContainer;

public class IDEtalkContainerRegistry {
  private final MutablePicoContainer myProjectContainer = Pico.getInstance().makeChildContainer();

  public MutablePicoContainer getContainer() {
    return myProjectContainer;
  }
}
