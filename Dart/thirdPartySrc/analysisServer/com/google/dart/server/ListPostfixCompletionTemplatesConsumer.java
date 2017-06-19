package com.google.dart.server;

import org.dartlang.analysis.server.protocol.PostfixCompletionTemplate;
import org.dartlang.analysis.server.protocol.RequestError;

public interface ListPostfixCompletionTemplatesConsumer extends Consumer {

  public void postfixCompletionTemplates(PostfixCompletionTemplate[] templates);

  public void onError(RequestError requestError);
}
