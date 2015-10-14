package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.jstestdriver.idea.common.JsErrorMessage;
import com.google.jstestdriver.idea.rt.execution.tc.TC;
import com.google.jstestdriver.idea.rt.execution.tc.TCAttribute;
import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
import com.google.jstestdriver.idea.rt.util.EscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class BrowserErrorNode extends AbstractNodeWithParent<BrowserErrorNode> {

  private final JsErrorMessage myErrorMessage;

  private BrowserErrorNode(@NotNull BrowserNode parent, @Nullable JsErrorMessage errorMessage) {
    super(errorMessage != null ? errorMessage.getErrorName() : "Error", parent);
    myErrorMessage = errorMessage;
  }

  @NotNull
  @Override
  public BrowserNode getParent() {
    return (BrowserNode) super.getParent();
  }

  @Override
  public String getProtocolId() {
    return "browserError";
  }

  @Override
  public String getLocationPath() {
    if (myErrorMessage == null) {
      return null;
    }
    Integer columnNumber = myErrorMessage.getColumnNumber();
    List<String> components = Arrays.asList(
      myErrorMessage.getFileWithError().getAbsolutePath(),
      String.valueOf(myErrorMessage.getLineNumber()),
      columnNumber != null ? columnNumber.toString() : ""
    );
    return EscapeUtils.join(components, ':');
  }

  @NotNull
  @Override
  public TCMessage createStartedMessage() {
    TCMessage message = TC.newBrowserErrorStartedMessage(this);
    ConfigNode configNode = getParent().getParent();
    String basePath = configNode.getAbsoluteBasePath();
    if (basePath != null) {
      message.addAttribute(TCAttribute.NODE_TYPE, "browserError");
      message.addAttribute(TCAttribute.NODE_ARGS, basePath);
    }
    return message;
  }

  @NotNull
  public static BrowserErrorNode newBrowserErrorNode(@NotNull BrowserNode parent,
                                                     @Nullable String pathToJsFileWithError,
                                                     @Nullable String errorMessage) {
    ConfigNode configNode = parent.getParent();
    String basePath = configNode.getAbsoluteBasePath();
    final JsErrorMessage parsedErrorMessage;
    if (basePath != null && errorMessage != null) {
      parsedErrorMessage = JsErrorMessage.parseFromText(errorMessage, new File(basePath));
    } else {
      parsedErrorMessage = null;
    }
    JsErrorMessage result = null;
    if (pathToJsFileWithError == null) {
      result = parsedErrorMessage;
    } else {
      File file = new File(pathToJsFileWithError);
      if (file.isAbsolute() && file.isFile()) {
        if (parsedErrorMessage != null && parsedErrorMessage.getFileWithError().equals(file)) {
          result = parsedErrorMessage;
        } else {
          result = new JsErrorMessage(file, 1, null, false, null, -1, -1);
        }
      }
    }
    return new BrowserErrorNode(parent, result);
  }

}
