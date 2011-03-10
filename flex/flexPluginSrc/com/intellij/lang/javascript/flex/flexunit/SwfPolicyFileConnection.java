package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import com.intellij.util.ResourceUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class SwfPolicyFileConnection extends ServerConnectionBase {

  private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

  public static final int DEFAULT_PORT = 843;

  private static final Logger LOG = Logger.getInstance(FlexUnitConnection.class.getName());

  private final String myContent;

  public SwfPolicyFileConnection() throws ExecutionException {
    final URL resource = FlexUnitRunConfiguration.class.getResource("SocketPolicyFile.xml");
    try {
      myContent = ResourceUtil.loadText(resource);
    }
    catch (IOException e) {
      throw new ExecutionException(e.getMessage(), e);
    }

  }

  protected void run(InputStream inputStream) throws IOException {
    InputStreamReader reader = new InputStreamReader(inputStream);

    BufferExposingByteArrayOutputStream buffer = new BufferExposingByteArrayOutputStream(100);
    while (!isStopped()) {
      int i = reader.read();
      if (i == -1) return;

      if (i == 0) break;
      buffer.write(i);
    }
    final String request = new String(buffer.getInternalBuffer(), 0, buffer.size());
    LOG.debug("Policy file request: " + request);

    if (POLICY_FILE_REQUEST.equals(request)) {
      write(myContent);
    }
  }

}
