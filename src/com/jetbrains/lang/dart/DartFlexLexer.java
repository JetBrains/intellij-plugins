package com.jetbrains.lang.dart;

import com.intellij.diagnostic.LogMessageEx;
import com.intellij.diagnostic.errordialog.Attachment;
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
  public void advance() {
    try {
      super.advance();
    }
    catch (Error e) {
      final Attachment attachment = new Attachment("error.dart", getBufferSequence().toString());
      LOG.error(LogMessageEx.createEvent(e.getMessage(), ExceptionUtil.getThrowableText(e), attachment));
    }
  }
}
