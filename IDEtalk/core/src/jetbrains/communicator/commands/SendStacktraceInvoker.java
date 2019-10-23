// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.users.User;

/**
 * @author Kir
 */
public interface SendStacktraceInvoker {
  void doSendStacktrace(User[] targetUsers, String stacktrace, String comment);
}
