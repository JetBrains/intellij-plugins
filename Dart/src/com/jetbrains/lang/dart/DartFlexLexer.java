package com.jetbrains.lang.dart;

import com.intellij.diagnostic.LogMessageEx;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.lexer.FlexAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ExceptionUtil;

import java.io.Reader;

/**
 * @author: Fedor.Korotkov
 */
public class DartFlexLexer extends FlexAdapter {
  private final static Logger LOG = Logger.getInstance(DartFlexLexer.class);

  public DartFlexLexer() {
    super(new _DartLexer((Reader)null));
  }

  @Override
  public void locateToken() {
    try {
      super.locateToken();
    }
    catch (Error e) {
      final Attachment attachment = new Attachment("error.dart", getBufferSequence().toString());
      LOG.error(LogMessageEx.createEvent(e.getMessage(), ExceptionUtil.getThrowableText(e), attachment));
      throw new Error(e);
    }
  }
}
