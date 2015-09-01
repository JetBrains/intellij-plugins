package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.HbBundle;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

import java.util.HashSet;
import java.util.Set;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

/**
 * The parser is based directly on Handlebars.yy
 * (taken from the following revision: https://github.com/wycats/handlebars.js/blob/b8a9f7264d3b6ac48514272bf35291736cedad00/src/handlebars.yy)
 * <p/>
 * Methods mapping to expression in the grammar are commented with the part of the grammar they map to.
 * <p/>
 * Places where we've gone off book to make the live syntax detection a more pleasant experience are
 * marked HB_CUSTOMIZATION.  If we find bugs, or the grammar is ever updated, these are the first candidates to check.
 */
public class HbParsing {
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
    while (!builder.eof()) {
      parseRoot(builder);

      if (builder.eof()) {
        break;
      }
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
    }
  }

  /**
   * root
   * : program EOF
   */
  private void parseRoot(PsiBuilder builder) {
    parseProgram(builder);
  }

  /**
   * program
   * : statement*
   * | ""
   * ;
   */
  private void parseProgram(PsiBuilder builder) {
    parseStatements(builder);
  }

  /**
   * statement*
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
   * : block
   * | mustache (HB_CUSTOMIZATION we check `block` before `mustache` because our custom "{{else" gets incorrectly parsed as a broken
   * mustache if we parse this first)
   * | rawBlock
   * | partial
   * | ESCAPE_CHAR (HB_CUSTOMIZATION the official Handlebars lexer just throws out the escape char;
   * it's convenient for us to keep it so that we can highlight it)
   * | CONTENT
   * | COMMENT
   * ;
   */
  private boolean parseStatement(PsiBuilder builder) {
    IElementType tokenType = builder.getTokenType();

    /**
     * block
     * : openBlock program inverseAndChain? closeBlock
     * | openInverse program inverseAndProgram? closeBlock
     */
    {
      if (builder.getTokenType() == OPEN_INVERSE) {
        if (builder.lookAhead(1) == CLOSE) {
                /* HB_CUSTOMIZATION */
          // this is actually a `{{^}}` simple inverse.  Bail out.  It gets parsed outside of `statement`
          return false;
        }

        PsiBuilder.Marker blockMarker = builder.mark();
        if (parseOpenInverse(builder)) {
          parseProgram(builder);
          parseInverseAndProgram(builder);
          parseCloseBlock(builder);
          blockMarker.done(BLOCK_WRAPPER);
        }
        else {
          return false;
        }

        return true;
      }

      if (tokenType == OPEN_BLOCK) {
        PsiBuilder.Marker blockMarker = builder.mark();
        if (parseOpenBlock(builder)) {
          parseProgram(builder);
          parseInverseChain(builder);
          parseCloseBlock(builder);
          blockMarker.done(BLOCK_WRAPPER);
        }
        else {
          return false;
        }

        return true;
      }
    }

    /**
     * mustache
     * : OPEN sexpr CLOSE
     * | OPEN_UNESCAPED sexpr CLOSE_UNESCAPED
     * ;
     */
    {
      if (tokenType == OPEN) {
        if (builder.lookAhead(1) == ELSE) {
                /* HB_CUSTOMIZATION */
          // this is actually an `{{else` expression, not a mustache.
          return false;
        }

        parseMustache(builder, OPEN, CLOSE);
        return true;
      }

      if (tokenType == OPEN_UNESCAPED) {
        parseMustache(builder, OPEN_UNESCAPED, CLOSE_UNESCAPED);
        return true;
      }
    }

    /**
     * rawBlock
     * : openRawBlock CONTENT endRawBlock
     */
    if (tokenType == OPEN_RAW_BLOCK) {
      PsiBuilder.Marker blockMarker = builder.mark();
      if (parseOpenRawBlock(builder)) {
        if (builder.getTokenType() == CONTENT) {
          builder.advanceLexer(); // eat non-HB content
        }
        parseCloseRawBlock(builder);
        blockMarker.done(BLOCK_WRAPPER);
      }
      else {
        return false;
      }

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
   * inverseChain
   * : openInverseChain program inverseChain?
   * | inverseAndProgram
   */
  private void parseInverseChain(PsiBuilder builder) {
    if (!parseInverseAndProgram(builder)) {
      if (parseOpenInverseChain(builder)) {
        parseProgram(builder);
        parseInverseChain(builder);
      }
    }
  }

  /**
   * inverseAndProgram
   * : INVERSE program
   */
  private boolean parseInverseAndProgram(PsiBuilder builder) {
    if (parseINVERSE(builder)) {
      parseProgram(builder);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * openRawBlock
   * : OPEN_RAW_BLOCK sexpr CLOSE_RAW_BLOCK
   */
  private boolean parseOpenRawBlock(PsiBuilder builder) {
    PsiBuilder.Marker openRawBlockStacheMarker = builder.mark();
    if (!parseLeafToken(builder, OPEN_RAW_BLOCK)) {
      openRawBlockStacheMarker.drop();
      return false;
    }

    if (parseSexpr(builder)) {
      parseLeafTokenGreedy(builder, CLOSE_RAW_BLOCK);
    }

    openRawBlockStacheMarker.done(OPEN_BLOCK_STACHE);
    return true;
  }

  /**
   * openBlock
   * : OPEN_BLOCK sexpr blockParams? CLOSE { $$ = new yy.MustacheNode($2[0], $2[1]); }
   * ;
   */
  private boolean parseOpenBlock(PsiBuilder builder) {
    PsiBuilder.Marker openBlockStacheMarker = builder.mark();
    if (!parseLeafToken(builder, OPEN_BLOCK)) {
      openBlockStacheMarker.drop();
      return false;
    }

    if (parseSexpr(builder)) {
      parseBlockParams(builder);
      parseLeafTokenGreedy(builder, CLOSE);
    }

    openBlockStacheMarker.done(OPEN_BLOCK_STACHE);
    return true;
  }

  /**
   * openInverseChain
   * : OPEN_INVERSE_CHAIN sexpr CLOSE
   * ;
   */
  private boolean parseOpenInverseChain(PsiBuilder builder) {
    PsiBuilder.Marker openInverseChainMarker = builder.mark();
    if (!parseLeafToken(builder, OPEN)
        || !parseLeafToken(builder, ELSE)) {
      openInverseChainMarker.rollbackTo();
      return false;
    }

    if (parseSexpr(builder)) {
      parseLeafTokenGreedy(builder, CLOSE);
    }

    openInverseChainMarker.done(OPEN_INVERSE_CHAIN);

    return true;
  }

  /**
   * openInverse
   * : OPEN_INVERSE sexpr blockParams? CLOSE
   * ;
   */
  private boolean parseOpenInverse(PsiBuilder builder) {
    PsiBuilder.Marker openInverseBlockStacheMarker = builder.mark();

    if (!parseLeafToken(builder, OPEN_INVERSE)) {
      return false;
    }

    if (parseSexpr(builder)) {
      parseBlockParams(builder);
      parseLeafTokenGreedy(builder, CLOSE);
    }

    openInverseBlockStacheMarker.done(OPEN_INVERSE_BLOCK_STACHE);
    return true;
  }

  /**
   * closeRawBlock
   * : END_RAW_BLOCK path CLOSE_RAW_BLOCK
   * ;
   */
  private boolean parseCloseRawBlock(PsiBuilder builder) {
    PsiBuilder.Marker closeRawBlockMarker = builder.mark();

    if (!parseLeafToken(builder, END_RAW_BLOCK)) {
      closeRawBlockMarker.drop();
      return false;
    }

    PsiBuilder.Marker mustacheNameMark = builder.mark();
    parsePath(builder);
    mustacheNameMark.done(MUSTACHE_NAME);
    parseLeafTokenGreedy(builder, CLOSE_RAW_BLOCK);
    closeRawBlockMarker.done(CLOSE_BLOCK_STACHE);
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

    PsiBuilder.Marker mustacheNameMark = builder.mark();
    parsePath(builder);
    mustacheNameMark.done(MUSTACHE_NAME);
    parseLeafTokenGreedy(builder, CLOSE);
    closeBlockMarker.done(CLOSE_BLOCK_STACHE);
    return true;
  }

  /**
   * mustache
   * : OPEN sexpr CLOSE
   * | OPEN_UNESCAPED sexpr CLOSE_UNESCAPED
   * ;
   */
  protected void parseMustache(PsiBuilder builder, IElementType openStache, IElementType closeStache) {
    PsiBuilder.Marker mustacheMarker = builder.mark();
    parseLeafToken(builder, openStache);
    parseSexpr(builder);
    parseLeafTokenGreedy(builder, closeStache);
    mustacheMarker.done(MUSTACHE);
  }

  /**
   * partial
   * : OPEN_PARTIAL partialName param hash? CLOSE
   * | OPEN_PARTIAL partialName hash? CLOSE
   * ;
   */
  protected void parsePartial(PsiBuilder builder) {
    PsiBuilder.Marker partialMarker = builder.mark();

    parseLeafToken(builder, OPEN_PARTIAL);

    parsePartialName(builder);

    // parse the optional param
    PsiBuilder.Marker optionalParamMarker = builder.mark();
    if (parseParam(builder)) {
      optionalParamMarker.drop();
    }
    else {
      optionalParamMarker.rollbackTo();
    }

    // parse the optional hash
    PsiBuilder.Marker optionalHashMarker = builder.mark();
    if (parseHash(builder)) {
      optionalHashMarker.drop();
    }
    else {
      optionalHashMarker.rollbackTo();
    }

    parseLeafTokenGreedy(builder, CLOSE);

    partialMarker.done(PARTIAL_STACHE);
  }

  /**
   * HB_CUSTOMIZATION: we don't parse an INVERSE token like the wycats/handlebars grammar since we lex "else" as
   * an individual token so we can highlight it distinctly.  This method parses {{^}} and {{else}}
   * as a unit to synthesize INVERSE
   */
  private boolean parseINVERSE(PsiBuilder builder) {
    PsiBuilder.Marker simpleInverseMarker = builder.mark();
    boolean isSimpleInverse;

    // try and parse "{{^}}"
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

    // if we didn't find "{{^}}", check for "{{else}}"
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
   * sexpr
   * : path params hash
   * | path params
   * | path hash
   * | path
   * | dataName
   * ;
   */
  protected boolean parseSexpr(PsiBuilder builder) {
    PsiBuilder.Marker sexprMarker = builder.mark();
    PsiBuilder.Marker mustacheNameMarker = builder.mark();

    if (!parsePath(builder)) {
      // not a path, try to parse dataName
      if (parseDataName(builder)) {
        mustacheNameMarker.done(MUSTACHE_NAME);
        sexprMarker.drop();
        return true;
      }
      else {
        mustacheNameMarker.drop();
        sexprMarker.error(HbBundle.message("hb.parsing.expected.path.or.data"));
        return false;
      }
    }

    mustacheNameMarker.done(MUSTACHE_NAME);

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

    sexprMarker.drop();
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
   * | NUMBER
   * | BOOLEAN
   * | dataName
   * | OPEN_SEXPR sexpr CLOSE_SEXPR
   * ;
   */
  protected boolean parseParam(PsiBuilder builder) {
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
    if (parseLeafToken(builder, NUMBER)) {
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
    if (parseDataName(builder)) {
      dataMarker.drop();
      paramMarker.done(PARAM);
      return true;
    }
    else {
      dataMarker.rollbackTo();
    }

    PsiBuilder.Marker sexprMarker = builder.mark();
    if (parseLeafToken(builder, OPEN_SEXPR)) {
      parseSexpr(builder);
      parseLeafTokenGreedy(builder, CLOSE_SEXPR);
      sexprMarker.drop();
      paramMarker.done(PARAM);
      return true;
    }
    else {
      sexprMarker.rollbackTo();
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
   * | ID EQUALS NUMBER
   * | ID EQUALS BOOLEAN
   * | ID EQUALS dataName
   * ;
   * <p/>
   * Refactored to:
   * hashSegment
   * : ID EQUALS param
   */
  private boolean parseHashSegment(PsiBuilder builder) {
    final PsiBuilder.Marker hash = builder.mark();

    boolean result = parseLeafToken(builder, ID)
                     && parseLeafToken(builder, EQUALS)
                     && parseParam(builder);

    if (result) {
      hash.done(HASH);
    }
    else {
      hash.drop();
    }

    return result;
  }

  /**
   * partialName
   * : path
   * | STRING
   * | NUMBER
   * ;
   */
  private boolean parsePartialName(PsiBuilder builder) {
    PsiBuilder.Marker partialNameMarker = builder.mark();

    PsiBuilder.Marker pathMarker = builder.mark();
    if (parsePath(builder)) {
      pathMarker.drop();
      partialNameMarker.done(PARTIAL_NAME);
      return true;
    }
    else {
      pathMarker.rollbackTo();
    }

    PsiBuilder.Marker stringMarker = builder.mark();
    if (parseLeafToken(builder, STRING)) {
      stringMarker.drop();
      partialNameMarker.done(PARTIAL_NAME);
      return true;
    }
    else {
      stringMarker.rollbackTo();
    }

    PsiBuilder.Marker integerMarker = builder.mark();
    if (parseLeafToken(builder, NUMBER)) {
      integerMarker.drop();
      partialNameMarker.done(PARTIAL_NAME);
      return true;
    }
    else {
      integerMarker.rollbackTo();
    }

    partialNameMarker.error(HbBundle.message("hb.parsing.expected.partial.name"));
    return false;
  }

  /**
   * blockParams
   * OPEN_BLOCK_PARAMS ID+ CLOSE_BLOCK_PARAMS
   */
  private boolean parseBlockParams(PsiBuilder builder) {
    PsiBuilder.Marker blockParamsMarker = builder.mark();
    if (parseLeafToken(builder, OPEN_BLOCK_PARAMS)) {
      blockParamsMarker.drop();
      parseLeafToken(builder, ID);
      // parse any additional IDs
      while (true) {
        PsiBuilder.Marker optionalIdMarker = builder.mark();
        if (parseLeafToken(builder, ID)) {
          optionalIdMarker.drop();
        }
        else {
          optionalIdMarker.rollbackTo();
          break;
        }
      }
      parseLeafToken(builder, CLOSE_BLOCK_PARAMS);
      return true;
    }
    else {
      blockParamsMarker.rollbackTo();
      return false;
    }
  }

  /**
   * dataName
   * : DATA path
   * ;
   */
  private boolean parseDataName(PsiBuilder builder) {
    PsiBuilder.Marker prefixMarker = builder.mark();
    if (parseLeafToken(builder, DATA_PREFIX)) {
      prefixMarker.drop();
    }
    else {
      prefixMarker.rollbackTo();
      return false;
    }

    PsiBuilder.Marker dataMarker = builder.mark();
    if (parsePath(builder)) {
      dataMarker.done(DATA);
      return true;
    }

    dataMarker.rollbackTo();
    return false;
  }

  /**
   * path
   * : pathSegments { $$ = new yy.IdNode($1); }
   * ;
   */
  protected boolean parsePath(PsiBuilder builder) {
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
  protected boolean parsePathSegments(PsiBuilder builder) {
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
  protected void parsePathSegmentsPrime(PsiBuilder builder) {
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
  protected boolean isHashNextLookAhead(PsiBuilder builder) {
    PsiBuilder.Marker hashLookAheadMarker = builder.mark();
    boolean isHashUpcoming = parseHashSegment(builder);
    hashLookAheadMarker.rollbackTo();
    return isHashUpcoming;
  }

  /**
   * Tries to parse the given token, marking an error if any other token is found
   */
  protected boolean parseLeafToken(PsiBuilder builder, IElementType leafTokenType) {
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
  protected void parseLeafTokenGreedy(PsiBuilder builder, IElementType expectedToken) {
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

    if (!builder.eof() && builder.getTokenType() == expectedToken) {
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
}
