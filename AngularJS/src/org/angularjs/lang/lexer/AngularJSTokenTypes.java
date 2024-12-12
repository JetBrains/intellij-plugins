package org.angularjs.lang.lexer;

/**
 * @author Dennis.Ushakov
 */
public interface AngularJSTokenTypes {
  AngularJSTokenType ESCAPE_SEQUENCE = new AngularJSTokenType("ESCAPE_SEQUENCE");
  AngularJSTokenType INVALID_ESCAPE_SEQUENCE = new AngularJSTokenType("INVALID_ESCAPE_SEQUENCE");
  AngularJSTokenType TRACK_BY_KEYWORD = new AngularJSTokenType("TRACK_BY_KEYWORD");
  AngularJSTokenType ONE_TIME_BINDING = new AngularJSTokenType("ONE_TIME_BINDING");
  AngularJSTokenType ELVIS = new AngularJSTokenType("ELVIS");// ?.
  AngularJSTokenType ASSERT_NOT_NULL = new AngularJSTokenType("ASSERT_NOT_NULL");// !.
  AngularJSTokenType THEN = new AngularJSTokenType("THEN");
}
