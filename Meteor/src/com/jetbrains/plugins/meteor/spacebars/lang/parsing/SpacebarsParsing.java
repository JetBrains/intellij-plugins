package com.jetbrains.plugins.meteor.spacebars.lang.parsing;


import com.dmarcotte.handlebars.parsing.HbParsing;
import com.intellij.lang.PsiBuilder;

import static com.dmarcotte.handlebars.parsing.HbTokenTypes.*;

public class SpacebarsParsing extends HbParsing {
  public SpacebarsParsing(PsiBuilder builder) {
    super(builder);
  }


  /**
   * Copy of the super class method with additional logic for template like {{tName valueN .42}}
   * {@link #parseParam(PsiBuilder)}
   */
  @Override
  protected boolean parsePathSegments(PsiBuilder builder) {
    PsiBuilder.Marker pathSegmentsMarker = builder.mark();

    if (isHashNextLookAhead(builder)) {
      pathSegmentsMarker.rollbackTo();
      return false;
    }

    boolean hasWtsAfterID = builder.getTokenType() == ID && builder.rawLookup(1) == WHITE_SPACE;
    if (!parseLeafToken(builder, ID)) {
      pathSegmentsMarker.drop();
      return false;
    }

    //if we have a SEP with prev whitespaces we should try to read construction '{{name .42}}'
    //default logic consider {{tName valueN .42}} as {{tName valueN. 42}} and add notification 'there is no property'
    if (hasWtsAfterID) {
      if (builder.getTokenType() == SEP && builder.rawLookup(1) == NUMBER) {
        pathSegmentsMarker.drop();
        return true;
      }
    }

    /**/
    parsePathSegmentsPrime(builder);

    pathSegmentsMarker.drop();
    return true;
  }

  /**
   * Add parsing for number params like ".42" (handlebars already supports form like "42.42")
   */
  @Override
  protected boolean parseParam(PsiBuilder builder) {
    if (builder.getTokenType() == SEP && builder.rawLookup(1) == NUMBER) {
      PsiBuilder.Marker paramMarker = builder.mark();
      PsiBuilder.Marker mark = builder.mark();
      builder.advanceLexer();
      builder.advanceLexer();
      mark.done(NUMBER);
      paramMarker.done(PARAM);
      return true;
    }
    return super.parseParam(builder);
  }
}
