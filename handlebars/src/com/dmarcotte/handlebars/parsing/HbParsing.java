package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.exception.ShouldNotHappenException;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

import java.util.HashSet;
import java.util.Set;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

/**
 * The parser is based directly on Handlebars.yy
 * (taken from the following revision: https://github.com/wycats/handlebars.js/blob/2ea95ca08d47bb16ed79e8481c50a1c074dd676e/src/handlebars.yy)
 * <p/>
 * Methods mapping to expression in the grammar are commented with the part of the grammar they map to.
 * <p/>
 * Places where we've gone off book to make the live syntax detection a more pleasant experience are
 * marked HB_CUSTOMIZATION.  If we find bugs, or the grammar is ever updated, these are the first candidates to check.
 */
class HbParsing {
  private final PsiBuilder builder;

  // the set of tokens which, if we encounter them while in a bad state, we'll try to
  // resume parsing from them
  private static final Set<IElementType> RECOVERY_SET;

  static {
    RECOVERY_SET = new HashSet<IElementType>();
    RECOVERY_SET.add(OPEN);
    RECOVERY_SET.add(OPEN_BLOCK);
    RECOVERY_SET.add(OPEN_ENDBLOCK);
    RECOVERY_SET.add(OPEN_INVERSE);
    RECOVERY_SET.add(OPEN_PARTIAL);
    RECOVERY_SET.add(OPEN_UNESCAPED);
    RECOVERY_SET.add(CONTENT);
  }

  public HbParsing(final PsiBuilder builder) {
    this.builder = builder;
  }

  public void parse() {
    parseProgram(builder);

    if (!builder.eof()) {
      // jumped out of the parser prematurely... try and figure out what's tripping it up,
      // then jump back in

      // deal with some unexpected tokens
      IElementType tokenType = builder.getTokenType();
      int problemOffset = builder.getCurrentOffset();

      if (tokenType == OPEN_ENDBLOCK) {
        parseCloseBlock(builder);
      }

      if (builder.getCurrentOffset() == problemOffset) {
        // none of our error checks advanced the lexer, do it manually before we
        // try and resume parsing to avoid an infinite loop
        PsiBuilder.Marker problemMark = builder.mark();
        builder.advanceLexer();
        problemMark.error(HbBundle.message("hb.parsing.invalid"));
      }

      parse();
    }
  }

  /**
   * program
   * : statements simpleInverse statements
   * | statements
   * | ""
   * ;
   */
  private void parseProgram(PsiBuilder builder) {
    if (builder.eof()) {
      return;
    }

    parseStatements(builder);
    if (parseSimpleInverse(builder)) {
      // if we have a simple inverse, must have more statements
      parseStatements(builder);
    }
  }

  /**
   * statements
   * : statement
   * | statements statement
   * ;
   */
  private void parseStatements(PsiBuilder builder) {
    PsiBuilder.Marker statementsMarker = builder.mark();

    // parse zero or more statements (empty statements are acceptable)
    while (true) {
      PsiBuilder.Marker optionalStatementMarker = builder.mark();
      if (parseStatement(builder)) {
        optionalStatementMarker.drop();
      }
      else {
        optionalStatementMarker.rollbackTo();
        break;
      }
    }

    statementsMarker.done(STATEMENTS);
  }

  /**
   * statement
   * : openInverse program closeBlock
   * | openBlock program closeBlock
   * | mustache
   * | partial
   * | ESCAPE_CHAR CONTENT  (HB_CUSTOMIZATION the official Handlebars lexer just throws out the escape char;
   * it's convenient for us to keep it so that we can highlight it)
   * | CONTENT
   * | COMMENT
   * ;
   */
  private boolean parseStatement(PsiBuilder builder) {
    IElementType tokenType = builder.getTokenType();

    if (atOpenInverseExpression(builder)) {
      PsiBuilder.Marker inverseBlockStartMarker = builder.mark();
      PsiBuilder.Marker lookAheadMarker = builder.mark();
      boolean isSimpleInverse = parseSimpleInverse(builder);
      lookAheadMarker.rollbackTo();

      if (isSimpleInverse) {
                /* HB_CUSTOMIZATION */
        // leave this to be caught be the simpleInverseParser
        inverseBlockStartMarker.rollbackTo();
        return false;
      }
      else {
        inverseBlockStartMarker.drop();
      }

      PsiBuilder.Marker blockMarker = builder.mark();
      if (parseOpenInverse(builder)) {
        parseRestOfBlock(builder, blockMarker);
      }
      else {
        return false;
      }

      return true;
    }

    if (tokenType == OPEN_BLOCK) {
      PsiBuilder.Marker blockMarker = builder.mark();
      if (parseOpenBlock(builder)) {
        parseRestOfBlock(builder, blockMarker);
      }
      else {
        return false;
      }

      return true;
    }

    if (tokenType == OPEN || tokenType == OPEN_UNESCAPED) {
      parseMustache(builder);
      return true;
    }

    if (tokenType == OPEN_PARTIAL) {
      parsePartial(builder);
      return true;
    }

    if (tokenType == ESCAPE_CHAR) {
      builder.advanceLexer(); // ignore the escape character
      return true;
    }

    if (tokenType == CONTENT) {
      builder.advanceLexer(); // eat non-HB content
      return true;
    }

    if (tokenType == COMMENT) {
      parseLeafToken(builder, COMMENT);
      return true;
    }

    // HB_CUSTOMIZATION: we lex UNCLOSED_COMMENT sections specially so that we can coherently mark them as errors
    if (tokenType == UNCLOSED_COMMENT) {
      PsiBuilder.Marker unclosedCommentMarker = builder.mark();
      parseLeafToken(builder, UNCLOSED_COMMENT);
      unclosedCommentMarker.error(HbBundle.message("hb.parsing.comment.unclosed"));
      return true;
    }

    return false;
  }

  /**
   * Helper method to take care of the business needed after an "open-type mustache" (openBlock or openInverse)
   * <p/>
   * NOTE: will resolve the given blockMarker
   */
  private void parseRestOfBlock(PsiBuilder builder, PsiBuilder.Marker blockMarker) {
    parseProgram(builder);
    parseCloseBlock(builder);
    blockMarker.done(HbTokenTypes.BLOCK_WRAPPER);
  }

  /**
   * openBlock
   * : OPEN_BLOCK inMustache CLOSE { $$ = new yy.MustacheNode($2[0], $2[1]); }
   * ;
   */
  private boolean parseOpenBlock(PsiBuilder builder) {
    PsiBuilder.Marker openBlockStacheMarker = builder.mark();
    if (!parseLeafToken(builder, OPEN_BLOCK)) {
      openBlockStacheMarker.drop();
      return false;
    }

    if (parseInMustache(builder)) {
      parseLeafTokenGreedy(builder, CLOSE);
    }

    openBlockStacheMarker.done(OPEN_BLOCK_STACHE);
    return true;
  }

  /**
   * openInverse
   * : OPEN_INVERSE inMustache CLOSE
   * ;
   */
  private boolean parseOpenInverse(PsiBuilder builder) {
    PsiBuilder.Marker openInverseBlockStacheMarker = builder.mark();

    PsiBuilder.Marker regularInverseMarker = builder.mark();
    if (!parseLeafToken(builder, OPEN_INVERSE)) {
      // didn't find a standard open inverse token,
      // check for the "{{else" version
      regularInverseMarker.rollbackTo();
      if (!parseLeafToken(builder, OPEN)
          || !parseLeafToken(builder, ELSE)) {
        openInverseBlockStacheMarker.drop();
        return false;
      }
    }
    else {
      regularInverseMarker.drop();
    }

    if (parseInMustache(builder)) {
      parseLeafTokenGreedy(builder, CLOSE);
    }

    openInverseBlockStacheMarker.done(OPEN_INVERSE_BLOCK_STACHE);
    return true;
  }

  /**
   * closeBlock
   * : OPEN_ENDBLOCK path CLOSE { $$ = $2; }
   * ;
   */
  private boolean parseCloseBlock(PsiBuilder builder) {
    PsiBuilder.Marker closeBlockMarker = builder.mark();

    if (!parseLeafToken(builder, OPEN_ENDBLOCK)) {
      closeBlockMarker.drop();
      return false;
    }

    if (parsePath(builder)) {
      parseLeafToken(builder, CLOSE);
    }

    closeBlockMarker.done(CLOSE_BLOCK_STACHE);
    return true;
  }

  /**
   * mustache
   * : OPEN inMustache CLOSE { $$ = new yy.MustacheNode($2[0], $2[1]); }
   * | OPEN_UNESCAPED inMustache CLOSE { $$ = new yy.MustacheNode($2[0], $2[1], true); }
   * ;
   */
  private void parseMustache(PsiBuilder builder) {
    PsiBuilder.Marker mustacheMarker = builder.mark();
    if (builder.getTokenType() == OPEN) {
      parseLeafToken(builder, OPEN);
    }
    else if (builder.getTokenType() == OPEN_UNESCAPED) {
      parseLeafToken(builder, OPEN_UNESCAPED);
    }
    else {
      throw new ShouldNotHappenException();
    }

    parseInMustache(builder);
    // whether our parseInMustache hit trouble or not, we absolutely must have
    // a CLOSE token, so let's find it
    parseLeafTokenGreedy(builder, CLOSE);

    mustacheMarker.done(MUSTACHE);
  }

  /**
   * partial
   * : OPEN_PARTIAL PARTIAL_NAME CLOSE { $$ = new yy.PartialNode($2); }
   * | OPEN_PARTIAL PARTIAL_NAME path CLOSE { $$ = new yy.PartialNode($2, $3); }
   * ;
   */
  private void parsePartial(PsiBuilder builder) {
    PsiBuilder.Marker partialMarker = builder.mark();

    parseLeafToken(builder, OPEN_PARTIAL);

    parseLeafToken(builder, PARTIAL_NAME);

    // parse the optional path
    PsiBuilder.Marker optionalPathMarker = builder.mark();
    if (parsePath(builder)) {
      optionalPathMarker.drop();
    }
    else {
      optionalPathMarker.rollbackTo();
    }

    parseLeafTokenGreedy(builder, CLOSE);

    partialMarker.done(PARTIAL_STACHE);
  }

  /**
   * simpleInverse
   * : OPEN_INVERSE CLOSE
   * ;
   */
  private boolean parseSimpleInverse(PsiBuilder builder) {
    PsiBuilder.Marker simpleInverseMarker = builder.mark();
    boolean isSimpleInverse;

    // try and parse "{{^"
    PsiBuilder.Marker regularInverseMarker = builder.mark();
    if (!parseLeafToken(builder, OPEN_INVERSE)
        || !parseLeafToken(builder, CLOSE)) {
      regularInverseMarker.rollbackTo();
      isSimpleInverse = false;
    }
    else {
      regularInverseMarker.drop();
      isSimpleInverse = true;
    }

    // if we didn't find "{{^", check for "{{else"
    PsiBuilder.Marker elseInverseMarker = builder.mark();
    if (!isSimpleInverse
        && (!parseLeafToken(builder, OPEN)
            || !parseLeafToken(builder, ELSE)
            || !parseLeafToken(builder, CLOSE))) {
      elseInverseMarker.rollbackTo();
      isSimpleInverse = false;
    }
    else {
      elseInverseMarker.drop();
      isSimpleInverse = true;
    }

    if (isSimpleInverse) {
      simpleInverseMarker.done(SIMPLE_INVERSE);
      return true;
    }
    else {
      simpleInverseMarker.drop();
      return false;
    }
  }

  /**
   * inMustache
   * : path params hash { $$ = [[$1].concat($2), $3]; }
   * | path params { $$ = [[$1].concat($2), null]; }
   * | path hash { $$ = [[$1], $2]; }
   * | path { $$ = [[$1], null]; }
   * | DATA { $$ = [[new yy.DataNode($1)], null]; }
   * ;
   */
  private boolean parseInMustache(PsiBuilder builder) {
    PsiBuilder.Marker inMustacheMarker = builder.mark();

    if (!parsePath(builder)) {
      // not a path, try to parse DATA
      if (builder.getTokenType() == DATA_PREFIX
          && parseLeafToken(builder, DATA_PREFIX)
          && parseLeafToken(builder, HbTokenTypes.DATA)) {
        inMustacheMarker.drop();
        return true;
      }
      else {
        inMustacheMarker.error(HbBundle.message("hb.parsing.expected.path.or.data"));
        return false;
      }
    }

    // try to extend the 'path' we found to 'path hash'
    PsiBuilder.Marker hashMarker = builder.mark();
    if (parseHash(builder)) {
      hashMarker.drop();
    }
    else {
      // not a hash... try for 'path params', followed by an attempt at 'path params hash'
      hashMarker.rollbackTo();
      PsiBuilder.Marker paramsMarker = builder.mark();
      if (parseParams(builder)) {
        PsiBuilder.Marker paramsHashMarker = builder.mark();
        int hashStartPos = builder.getCurrentOffset();
        if (parseHash(builder)) {
          paramsHashMarker.drop();
        }
        else {
          if (hashStartPos < builder.getCurrentOffset()) {
                        /* HB_CUSTOMIZATION */
            // managed to partially parse the hash.  Don't rollback so that
            // we can keep the errors
            paramsHashMarker.drop();
          }
          else {
            paramsHashMarker.rollbackTo();
          }
        }
        paramsMarker.drop();
      }
      else {
        paramsMarker.rollbackTo();
      }
    }

    inMustacheMarker.drop();
    return true;
  }

  /**
   * params
   * : params param
   * | param
   * ;
   */
  private boolean parseParams(PsiBuilder builder) {
    PsiBuilder.Marker paramsMarker = builder.mark();

    if (!parseParam(builder)) {
      paramsMarker.error(HbBundle.message("hb.parsing.expected.parameter"));
      return false;
    }

    // parse any additional params
    while (true) {
      PsiBuilder.Marker optionalParamMarker = builder.mark();
      if (parseParam(builder)) {
        optionalParamMarker.drop();
      }
      else {
        optionalParamMarker.rollbackTo();
        break;
      }
    }

    paramsMarker.drop();
    return true;
  }

  /**
   * param
   * : path
   * | STRING
   * | INTEGER
   * | BOOLEAN
   * | DATA
   * ;
   */
  private boolean parseParam(PsiBuilder builder) {
    PsiBuilder.Marker paramMarker = builder.mark();

    if (parsePath(builder)) {
      paramMarker.done(PARAM);
      return true;
    }

    PsiBuilder.Marker stringMarker = builder.mark();
    if (parseLeafToken(builder, STRING)) {
      stringMarker.drop();
      paramMarker.done(PARAM);
      return true;
    }
    else {
      stringMarker.rollbackTo();
    }

    PsiBuilder.Marker integerMarker = builder.mark();
    if (parseLeafToken(builder, INTEGER)) {
      integerMarker.drop();
      paramMarker.done(PARAM);
      return true;
    }
    else {
      integerMarker.rollbackTo();
    }

    PsiBuilder.Marker booleanMarker = builder.mark();
    if (parseLeafToken(builder, BOOLEAN)) {
      booleanMarker.drop();
      paramMarker.done(PARAM);
      return true;
    }
    else {
      booleanMarker.rollbackTo();
    }

    PsiBuilder.Marker dataMarker = builder.mark();
    if (parseLeafToken(builder, DATA_PREFIX) && parseLeafToken(builder, DATA)) {
      dataMarker.drop();
      paramMarker.done(PARAM);
      return true;
    }
    else {
      dataMarker.rollbackTo();
    }

    paramMarker.error(HbBundle.message("hb.parsing.expected.parameter"));
    return false;
  }

  /**
   * hash
   * : hashSegments { $$ = new yy.HashNode($1); }
   * ;
   */
  private boolean parseHash(PsiBuilder builder) {
    return parseHashSegments(builder);
  }

  /**
   * hashSegments
   * : hashSegments hashSegment { $1.push($2); $$ = $1; }
   * | hashSegment { $$ = [$1]; }
   * ;
   */
  private boolean parseHashSegments(PsiBuilder builder) {
    PsiBuilder.Marker hashSegmentsMarker = builder.mark();

    if (!parseHashSegment(builder)) {
      hashSegmentsMarker.error(HbBundle.message("hb.parsing.expected.hash"));
      return false;
    }

    // parse any additional hash segments
    while (true) {
      PsiBuilder.Marker optionalHashMarker = builder.mark();
      int hashStartPos = builder.getCurrentOffset();
      if (parseHashSegment(builder)) {
        optionalHashMarker.drop();
      }
      else {
        if (hashStartPos < builder.getCurrentOffset()) {
          // HB_CUSTOMIZATION managed to partially parse this hash; don't roll back the errors
          optionalHashMarker.drop();
          hashSegmentsMarker.drop();
          return false;
        }
        else {
          optionalHashMarker.rollbackTo();
        }
        break;
      }
    }

    hashSegmentsMarker.drop();
    return true;
  }

  /**
   * hashSegment
   * : ID EQUALS path
   * | ID EQUALS STRING
   * | ID EQUALS INTEGER
   * | ID EQUALS BOOLEAN
   * ;
   * <p/>
   * Refactored to:
   * hashSegment
   * : ID EQUALS param
   */
  private boolean parseHashSegment(PsiBuilder builder) {
    return parseLeafToken(builder, ID)
           && parseLeafToken(builder, EQUALS)
           && parseParam(builder);
  }

  /**
   * path
   * : pathSegments { $$ = new yy.IdNode($1); }
   * ;
   */
  private boolean parsePath(PsiBuilder builder) {
    PsiBuilder.Marker pathMarker = builder.mark();
    if (parsePathSegments(builder)) {
      pathMarker.done(PATH);
      return true;
    }
    pathMarker.rollbackTo();
    return false;
  }

  /**
   * pathSegments
   * : pathSegments SEP ID { $1.push($3); $$ = $1; }
   * | ID { $$ = [$1]; }
   * ;
   * <p/>
   * Refactored to eliminate left recursion:
   * <p/>
   * pathSegments
   * : ID pathSegments'
   * <p/>
   * pathSegements'
   * : <epsilon>
   * | SEP ID pathSegments'
   */
  private boolean parsePathSegments(PsiBuilder builder) {
    PsiBuilder.Marker pathSegmentsMarker = builder.mark();

        /* HB_CUSTOMIZATION: see isHashNextLookAhead docs for details */
    if (isHashNextLookAhead(builder)) {
      pathSegmentsMarker.rollbackTo();
      return false;
    }

    if (!parseLeafToken(builder, ID)) {
      pathSegmentsMarker.drop();
      return false;
    }

    parsePathSegmentsPrime(builder);

    pathSegmentsMarker.drop();
    return true;
  }

  /**
   * See {@link #parsePathSegments(com.intellij.lang.PsiBuilder)} for more info on this method
   */
  private void parsePathSegmentsPrime(PsiBuilder builder) {
    PsiBuilder.Marker pathSegmentsPrimeMarker = builder.mark();

    if (!parseLeafToken(builder, SEP)) {
      // the epsilon case
      pathSegmentsPrimeMarker.rollbackTo();
      return;
    }

        /* HB_CUSTOMIZATION*/
    if (isHashNextLookAhead(builder)) {
      pathSegmentsPrimeMarker.rollbackTo();
      return;
    }

    if (parseLeafToken(builder, ID)) {
      parsePathSegmentsPrime(builder);
    }

    pathSegmentsPrimeMarker.drop();
  }

  /**
   * HB_CUSTOMIZATION: the beginnings of a 'hash' have a bad habit of looking like params
   * (i.e. test="what" parses as if "test" was a param, and then the builder is left pointing
   * at "=" which matches no rules).
   * <p/>
   * We check this in a couple of places to determine whether something should be parsed as
   * a param, or left alone to grabbed by the hash parser later
   */
  private boolean isHashNextLookAhead(PsiBuilder builder) {
    PsiBuilder.Marker hashLookAheadMarker = builder.mark();
    boolean isHashUpcoming = parseHashSegment(builder);
    hashLookAheadMarker.rollbackTo();
    return isHashUpcoming;
  }

  /**
   * Tries to parse the given token, marking an error if any other token is found
   */
  private boolean parseLeafToken(PsiBuilder builder, IElementType leafTokenType) {
    PsiBuilder.Marker leafTokenMark = builder.mark();
    if (builder.getTokenType() == leafTokenType) {
      builder.advanceLexer();
      leafTokenMark.done(leafTokenType);
      return true;
    }
    else if (builder.getTokenType() == INVALID) {
      while (!builder.eof() && builder.getTokenType() == INVALID) {
        builder.advanceLexer();
      }
      recordLeafTokenError(INVALID, leafTokenMark);
      return false;
    }
    else {
      recordLeafTokenError(leafTokenType, leafTokenMark);
      return false;
    }
  }

  /**
   * HB_CUSTOMIZATION
   * <p/>
   * Eats tokens until it finds the expected token, marking errors along the way.
   * <p/>
   * Will also stop if it encounters a {@link #RECOVERY_SET} token
   */
  @SuppressWarnings("SameParameterValue") // though this method is only being used for CLOSE right now, it reads better this way
  private void parseLeafTokenGreedy(PsiBuilder builder, IElementType expectedToken) {
    // failed to parse expected token... chew up tokens marking this error until we encounter
    // a token which give the parser a good shot at resuming
    if (builder.getTokenType() != expectedToken) {
      PsiBuilder.Marker unexpectedTokensMarker = builder.mark();
      while (!builder.eof()
             && builder.getTokenType() != expectedToken
             && !RECOVERY_SET.contains(builder.getTokenType())) {
        builder.advanceLexer();
      }

      recordLeafTokenError(expectedToken, unexpectedTokensMarker);
    }

    if (builder.getTokenType() == expectedToken) {
      parseLeafToken(builder, expectedToken);
    }
  }

  private void recordLeafTokenError(IElementType expectedToken, PsiBuilder.Marker unexpectedTokensMarker) {
    if (expectedToken instanceof HbElementType) {
      unexpectedTokensMarker.error(((HbElementType)expectedToken).parseExpectedMessage());
    }
    else {
      unexpectedTokensMarker.error(HbBundle.message("hb.parsing.element.expected.invalid"));
    }
  }

  /**
   * Helper method to check whether the builder is an open inverse expression.
   * <p/>
   * An open inverse expression is either an OPEN_INVERSE token (i.e. "{{^"), or
   * and OPEN token followed immediate by an ELSE token (i.e. "{{else")
   */
  private boolean atOpenInverseExpression(PsiBuilder builder) {
    boolean atOpenInverse = false;

    if (builder.getTokenType() == OPEN_INVERSE) {
      atOpenInverse = true;
    }

    PsiBuilder.Marker lookAheadMarker = builder.mark();
    if (builder.getTokenType() == OPEN) {
      builder.advanceLexer();
      if (builder.getTokenType() == ELSE) {
        atOpenInverse = true;
      }
    }

    lookAheadMarker.rollbackTo();
    return atOpenInverse;
  }
}
