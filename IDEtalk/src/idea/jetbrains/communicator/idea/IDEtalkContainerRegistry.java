// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import jetbrains.communicator.core.Pico;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

public class IDEtalkContainerRegistry {
  private final DefaultPicoContainer myProjectContainer = Pico.getInstance().makeChildContainer();

  public MutablePicoContainer getContainer() {
    return myProjectContainer;
  }
}
