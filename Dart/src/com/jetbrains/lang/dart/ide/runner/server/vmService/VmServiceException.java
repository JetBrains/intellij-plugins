package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.util.text.StringUtil;
import org.dartlang.vm.service.element.RPCError;
import org.jetbrains.annotations.NotNull;

public class VmServiceException extends Exception {
  public VmServiceException(String message) {
    super(message);
  }

  public VmServiceException(@NotNull final String request, @NotNull final RPCError error) {
    super(request + " request failed with error " + error.getCode() + ": " + error.getMessage() +
          (StringUtil.isEmpty(error.getDetails()) ? "" : "\n" + "Details: " + error.getDetails()));
  }
}
