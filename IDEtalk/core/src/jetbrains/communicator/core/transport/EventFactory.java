// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.util.StacktraceExtractor;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 */
@NonNls
public final class EventFactory {
  private EventFactory(){}

  @SuppressWarnings({"MethodWithTooManyParameters"})
  public static CodePointerEvent createCodePointerEvent(Transport transport, String remoteUser, VFile file, int line1, int col1, int line2, int col2, String comment) {
    return new CodePointerEvent(transport, remoteUser, file, line1, col1, line2, col2, comment);
  }

  public static StacktraceEvent createStacktraceEvent(Transport transport, String remoteUser, String stacktrace, String comment) {
    return new StacktraceEvent(transport, remoteUser, stacktrace, comment);
  }

  public static MessageEvent createMessageEvent(Transport transport, String remoteUser, String comment) {
    StacktraceExtractor stacktraceExtractor = new StacktraceExtractor(comment);
    if (stacktraceExtractor.containsStacktrace()) {
      return createStacktraceEvent(transport, remoteUser,
          stacktraceExtractor.getStacktrace(), stacktraceExtractor.getMessageText());
    }
    return new MessageEvent(transport, remoteUser, comment);
  }
}
