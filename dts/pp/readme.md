## Lexer and Parser
1. Create an object which implements the `PpTokenTypes` class (see `DtsPpTokenTypes`)
2. Emit the marker token `PpTokenTypes.statementMarker` from the host lexer (see `dts.flex`)
3. Create two lexers based on the lexer of the host language:
  - One lexer used for highlighting using the `PpHighlightingLexerAdapter` (see `DtsHighlightingLexerAdapter`)
  - One lexer used for parsing using the `PpParserLexerAdapter` (see `DtsParserLexerAdapter`)
4. Modify the parser utils class for the host language: (see `DtsJavaParserUtil`)
   - Extend `PpParserUtilBase`
   - Override the static `adapt_builder_` method (only possible in java) and return a new instance of the `PpBuildAdapter`
5. Modify the `ParserDefinition` for the host language: (see `DtsParserDefinition`)
  - *createLexer*: return an instance of the parsing lexer
  - *getCommentTokens*: add the inactive token type to this set
  - *createElement*: use the `createElement` function of the token types object
6. It is possible to provide a custom PsiElement by overriding the `statementElementFactory` function of `PpTokenTypes` (optional)

## Highlighting and inspections
1. Use the highlighting lexer in the host syntax highlighter (see `DtsSyntaxHighlighter`)
2. The preprocessor tokens from `PpTokenTypes` can be highlighted by the syntax highlighter of the host language 
3. Create an instance of the `PpHighlightAnnotator` and register it in the host language (see `DtsPpHighlightAnnotator`)
4. Register the `PpParserErrorInspection` in the host language