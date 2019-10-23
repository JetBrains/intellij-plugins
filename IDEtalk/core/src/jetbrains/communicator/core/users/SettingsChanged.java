// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.users;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkEvent;

/**
 * @author Kir
*/
public class SettingsChanged implements IDEtalkEvent {

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitSettingsChanged(this);
  }
}
