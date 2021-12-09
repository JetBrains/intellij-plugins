package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.vcs.impl.ProcessWaiter;
import org.jetbrains.idea.perforce.StreamGobbler;

import java.io.InputStream;

public class PerforceProcessWaiter extends ProcessWaiter<StreamGobbler> {
  @Override
  protected boolean tryReadStreams(int rc) {
    return rc != AbstractP4Connection.TIMEOUT_EXIT_CODE;
  }

  @Override
  protected StreamGobbler createStreamListener(InputStream stream) {
    return new StreamGobbler(stream);
  }

  public void clearGobblers() {
    if (myInStreamListener != null) {
      myInStreamListener.deleteTempFile();
    }
    if (myErrStreamListener != null) {
      myErrStreamListener.deleteTempFile();
    }
  }
}
