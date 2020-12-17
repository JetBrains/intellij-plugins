// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import com.intellij.util.ResourceUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class SwfPolicyFileConnection extends ServerConnectionBase {
  private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

  public static final int DEFAULT_PORT = 843;

  private static final Logger LOG = Logger.getInstance(FlexUnitConnection.class.getName());

  private final String myContent;

  public SwfPolicyFileConnection() throws ExecutionException {
    try (InputStream resource = getClass().getClassLoader().getResourceAsStream("com/intellij/lang/javascript/flex/flexunit/SocketPolicyFile.xml")) {
      myContent = ResourceUtil.loadText(Objects.requireNonNull(resource));
    }
    catch (IOException e) {
      throw new ExecutionException(e.getMessage(), e);
    }
  }

  @Override
  protected void run(InputStream inputStream) throws IOException {
    InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

    BufferExposingByteArrayOutputStream buffer = new BufferExposingByteArrayOutputStream(100);
    while (!isStopped()) {
      int i = reader.read();
      if (i == -1) return;

      if (i == 0) break;
      buffer.write(i);
    }
    final String request = new String(buffer.getInternalBuffer(), 0, buffer.size(), StandardCharsets.UTF_8);
    LOG.debug("Policy file request: " + request);

    if (POLICY_FILE_REQUEST.equals(request)) {
      write(myContent);
    }
  }

}
