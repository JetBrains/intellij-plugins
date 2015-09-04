package org.jetbrains.plugins.ruby.motion.run;

import java.util.concurrent.Semaphore;

/**
 * @author Dennis.Ushakov
 */
public interface ProcessHandlerWithDetachSemaphore {
  void setDetachSemaphore(Semaphore detachSemaphore);
}
