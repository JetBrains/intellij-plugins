/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.util.StacktraceExtractor;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 */
@NonNls
public class EventFactory {
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
