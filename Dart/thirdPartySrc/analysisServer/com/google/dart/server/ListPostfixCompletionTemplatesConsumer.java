package com.google.dart.server;

import org.dartlang.analysis.server.protocol.PostfixTemplateDescriptor;
import org.dartlang.analysis.server.protocol.RequestError;

public interface ListPostfixCompletionTemplatesConsumer extends Consumer {

  public void postfixCompletionTemplates(PostfixTemplateDescriptor[] templates);

  public void onError(RequestError requestError);
}
