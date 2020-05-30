// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.dispatcher;

import javax.swing.*;
import java.util.Date;

/**
 * @author Kir
 */
public interface LocalMessage extends Message {

  // TODO: move to IdeaLocalMessage
  Icon getMessageIcon(int refreshCounter);
  Date getWhen();

  /** This method is used when searching messages by pattern */
  boolean containsString(String searchString);
}
