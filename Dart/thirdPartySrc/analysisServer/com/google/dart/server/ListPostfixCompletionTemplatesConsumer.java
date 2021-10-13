package com.google.dart.server;

import org.dartlang.analysis.server.protocol.PostfixTemplateDescriptor;
import org.dartlang.analysis.server.protocol.RequestError;

public interface ListPostfixCompletionTemplatesConsumer extends Consumer {

  void postfixCompletionTemplates(PostfixTemplateDescriptor[] templates);

  void onError(RequestError requestError);
}
