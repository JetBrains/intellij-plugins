"use strict";
function init(ts_impl) {
    if (ts_impl.SyntaxKind) {
        return;
    }
    ts_impl.SyntaxKind = {};
    var SyntaxKind = ts_impl.SyntaxKind;
    SyntaxKind[SyntaxKind["Unknown"] = 0] = "Unknown";
    SyntaxKind[SyntaxKind["EndOfFileToken"] = 1] = "EndOfFileToken";
    SyntaxKind[SyntaxKind["SingleLineCommentTrivia"] = 2] = "SingleLineCommentTrivia";
    SyntaxKind[SyntaxKind["MultiLineCommentTrivia"] = 3] = "MultiLineCommentTrivia";
    SyntaxKind[SyntaxKind["NewLineTrivia"] = 4] = "NewLineTrivia";
    SyntaxKind[SyntaxKind["WhitespaceTrivia"] = 5] = "WhitespaceTrivia";
    // We detect and preserve #! on the first line
    SyntaxKind[SyntaxKind["ShebangTrivia"] = 6] = "ShebangTrivia";
    // We detect and provide better error recovery when we encounter a git merge marker.  This
    // allows us to edit files with git-conflict markers in them in a much more pleasant manner.
    SyntaxKind[SyntaxKind["ConflictMarkerTrivia"] = 7] = "ConflictMarkerTrivia";
    // Literals
    SyntaxKind[SyntaxKind["NumericLiteral"] = 8] = "NumericLiteral";
    SyntaxKind[SyntaxKind["StringLiteral"] = 9] = "StringLiteral";
    SyntaxKind[SyntaxKind["RegularExpressionLiteral"] = 10] = "RegularExpressionLiteral";
    SyntaxKind[SyntaxKind["NoSubstitutionTemplateLiteral"] = 11] = "NoSubstitutionTemplateLiteral";
    // Pseudo-literals
    SyntaxKind[SyntaxKind["TemplateHead"] = 12] = "TemplateHead";
    SyntaxKind[SyntaxKind["TemplateMiddle"] = 13] = "TemplateMiddle";
    SyntaxKind[SyntaxKind["TemplateTail"] = 14] = "TemplateTail";
    // Punctuation
    SyntaxKind[SyntaxKind["OpenBraceToken"] = 15] = "OpenBraceToken";
    SyntaxKind[SyntaxKind["CloseBraceToken"] = 16] = "CloseBraceToken";
    SyntaxKind[SyntaxKind["OpenParenToken"] = 17] = "OpenParenToken";
    SyntaxKind[SyntaxKind["CloseParenToken"] = 18] = "CloseParenToken";
    SyntaxKind[SyntaxKind["OpenBracketToken"] = 19] = "OpenBracketToken";
    SyntaxKind[SyntaxKind["CloseBracketToken"] = 20] = "CloseBracketToken";
    SyntaxKind[SyntaxKind["DotToken"] = 21] = "DotToken";
    SyntaxKind[SyntaxKind["DotDotDotToken"] = 22] = "DotDotDotToken";
    SyntaxKind[SyntaxKind["SemicolonToken"] = 23] = "SemicolonToken";
    SyntaxKind[SyntaxKind["CommaToken"] = 24] = "CommaToken";
    SyntaxKind[SyntaxKind["LessThanToken"] = 25] = "LessThanToken";
    SyntaxKind[SyntaxKind["LessThanSlashToken"] = 26] = "LessThanSlashToken";
    SyntaxKind[SyntaxKind["GreaterThanToken"] = 27] = "GreaterThanToken";
    SyntaxKind[SyntaxKind["LessThanEqualsToken"] = 28] = "LessThanEqualsToken";
    SyntaxKind[SyntaxKind["GreaterThanEqualsToken"] = 29] = "GreaterThanEqualsToken";
    SyntaxKind[SyntaxKind["EqualsEqualsToken"] = 30] = "EqualsEqualsToken";
    SyntaxKind[SyntaxKind["ExclamationEqualsToken"] = 31] = "ExclamationEqualsToken";
    SyntaxKind[SyntaxKind["EqualsEqualsEqualsToken"] = 32] = "EqualsEqualsEqualsToken";
    SyntaxKind[SyntaxKind["ExclamationEqualsEqualsToken"] = 33] = "ExclamationEqualsEqualsToken";
    SyntaxKind[SyntaxKind["EqualsGreaterThanToken"] = 34] = "EqualsGreaterThanToken";
    SyntaxKind[SyntaxKind["PlusToken"] = 35] = "PlusToken";
    SyntaxKind[SyntaxKind["MinusToken"] = 36] = "MinusToken";
    SyntaxKind[SyntaxKind["AsteriskToken"] = 37] = "AsteriskToken";
    SyntaxKind[SyntaxKind["AsteriskAsteriskToken"] = 38] = "AsteriskAsteriskToken";
    SyntaxKind[SyntaxKind["SlashToken"] = 39] = "SlashToken";
    SyntaxKind[SyntaxKind["PercentToken"] = 40] = "PercentToken";
    SyntaxKind[SyntaxKind["PlusPlusToken"] = 41] = "PlusPlusToken";
    SyntaxKind[SyntaxKind["MinusMinusToken"] = 42] = "MinusMinusToken";
    SyntaxKind[SyntaxKind["LessThanLessThanToken"] = 43] = "LessThanLessThanToken";
    SyntaxKind[SyntaxKind["GreaterThanGreaterThanToken"] = 44] = "GreaterThanGreaterThanToken";
    SyntaxKind[SyntaxKind["GreaterThanGreaterThanGreaterThanToken"] = 45] = "GreaterThanGreaterThanGreaterThanToken";
    SyntaxKind[SyntaxKind["AmpersandToken"] = 46] = "AmpersandToken";
    SyntaxKind[SyntaxKind["BarToken"] = 47] = "BarToken";
    SyntaxKind[SyntaxKind["CaretToken"] = 48] = "CaretToken";
    SyntaxKind[SyntaxKind["ExclamationToken"] = 49] = "ExclamationToken";
    SyntaxKind[SyntaxKind["TildeToken"] = 50] = "TildeToken";
    SyntaxKind[SyntaxKind["AmpersandAmpersandToken"] = 51] = "AmpersandAmpersandToken";
    SyntaxKind[SyntaxKind["BarBarToken"] = 52] = "BarBarToken";
    SyntaxKind[SyntaxKind["QuestionToken"] = 53] = "QuestionToken";
    SyntaxKind[SyntaxKind["ColonToken"] = 54] = "ColonToken";
    SyntaxKind[SyntaxKind["AtToken"] = 55] = "AtToken";
    // Assignments
    SyntaxKind[SyntaxKind["EqualsToken"] = 56] = "EqualsToken";
    SyntaxKind[SyntaxKind["PlusEqualsToken"] = 57] = "PlusEqualsToken";
    SyntaxKind[SyntaxKind["MinusEqualsToken"] = 58] = "MinusEqualsToken";
    SyntaxKind[SyntaxKind["AsteriskEqualsToken"] = 59] = "AsteriskEqualsToken";
    SyntaxKind[SyntaxKind["AsteriskAsteriskEqualsToken"] = 60] = "AsteriskAsteriskEqualsToken";
    SyntaxKind[SyntaxKind["SlashEqualsToken"] = 61] = "SlashEqualsToken";
    SyntaxKind[SyntaxKind["PercentEqualsToken"] = 62] = "PercentEqualsToken";
    SyntaxKind[SyntaxKind["LessThanLessThanEqualsToken"] = 63] = "LessThanLessThanEqualsToken";
    SyntaxKind[SyntaxKind["GreaterThanGreaterThanEqualsToken"] = 64] = "GreaterThanGreaterThanEqualsToken";
    SyntaxKind[SyntaxKind["GreaterThanGreaterThanGreaterThanEqualsToken"] = 65] = "GreaterThanGreaterThanGreaterThanEqualsToken";
    SyntaxKind[SyntaxKind["AmpersandEqualsToken"] = 66] = "AmpersandEqualsToken";
    SyntaxKind[SyntaxKind["BarEqualsToken"] = 67] = "BarEqualsToken";
    SyntaxKind[SyntaxKind["CaretEqualsToken"] = 68] = "CaretEqualsToken";
    // Identifiers
    SyntaxKind[SyntaxKind["Identifier"] = 69] = "Identifier";
    // Reserved words
    SyntaxKind[SyntaxKind["BreakKeyword"] = 70] = "BreakKeyword";
    SyntaxKind[SyntaxKind["CaseKeyword"] = 71] = "CaseKeyword";
    SyntaxKind[SyntaxKind["CatchKeyword"] = 72] = "CatchKeyword";
    SyntaxKind[SyntaxKind["ClassKeyword"] = 73] = "ClassKeyword";
    SyntaxKind[SyntaxKind["ConstKeyword"] = 74] = "ConstKeyword";
    SyntaxKind[SyntaxKind["ContinueKeyword"] = 75] = "ContinueKeyword";
    SyntaxKind[SyntaxKind["DebuggerKeyword"] = 76] = "DebuggerKeyword";
    SyntaxKind[SyntaxKind["DefaultKeyword"] = 77] = "DefaultKeyword";
    SyntaxKind[SyntaxKind["DeleteKeyword"] = 78] = "DeleteKeyword";
    SyntaxKind[SyntaxKind["DoKeyword"] = 79] = "DoKeyword";
    SyntaxKind[SyntaxKind["ElseKeyword"] = 80] = "ElseKeyword";
    SyntaxKind[SyntaxKind["EnumKeyword"] = 81] = "EnumKeyword";
    SyntaxKind[SyntaxKind["ExportKeyword"] = 82] = "ExportKeyword";
    SyntaxKind[SyntaxKind["ExtendsKeyword"] = 83] = "ExtendsKeyword";
    SyntaxKind[SyntaxKind["FalseKeyword"] = 84] = "FalseKeyword";
    SyntaxKind[SyntaxKind["FinallyKeyword"] = 85] = "FinallyKeyword";
    SyntaxKind[SyntaxKind["ForKeyword"] = 86] = "ForKeyword";
    SyntaxKind[SyntaxKind["FunctionKeyword"] = 87] = "FunctionKeyword";
    SyntaxKind[SyntaxKind["IfKeyword"] = 88] = "IfKeyword";
    SyntaxKind[SyntaxKind["ImportKeyword"] = 89] = "ImportKeyword";
    SyntaxKind[SyntaxKind["InKeyword"] = 90] = "InKeyword";
    SyntaxKind[SyntaxKind["InstanceOfKeyword"] = 91] = "InstanceOfKeyword";
    SyntaxKind[SyntaxKind["NewKeyword"] = 92] = "NewKeyword";
    SyntaxKind[SyntaxKind["NullKeyword"] = 93] = "NullKeyword";
    SyntaxKind[SyntaxKind["ReturnKeyword"] = 94] = "ReturnKeyword";
    SyntaxKind[SyntaxKind["SuperKeyword"] = 95] = "SuperKeyword";
    SyntaxKind[SyntaxKind["SwitchKeyword"] = 96] = "SwitchKeyword";
    SyntaxKind[SyntaxKind["ThisKeyword"] = 97] = "ThisKeyword";
    SyntaxKind[SyntaxKind["ThrowKeyword"] = 98] = "ThrowKeyword";
    SyntaxKind[SyntaxKind["TrueKeyword"] = 99] = "TrueKeyword";
    SyntaxKind[SyntaxKind["TryKeyword"] = 100] = "TryKeyword";
    SyntaxKind[SyntaxKind["TypeOfKeyword"] = 101] = "TypeOfKeyword";
    SyntaxKind[SyntaxKind["VarKeyword"] = 102] = "VarKeyword";
    SyntaxKind[SyntaxKind["VoidKeyword"] = 103] = "VoidKeyword";
    SyntaxKind[SyntaxKind["WhileKeyword"] = 104] = "WhileKeyword";
    SyntaxKind[SyntaxKind["WithKeyword"] = 105] = "WithKeyword";
    // Strict mode reserved words
    SyntaxKind[SyntaxKind["ImplementsKeyword"] = 106] = "ImplementsKeyword";
    SyntaxKind[SyntaxKind["InterfaceKeyword"] = 107] = "InterfaceKeyword";
    SyntaxKind[SyntaxKind["LetKeyword"] = 108] = "LetKeyword";
    SyntaxKind[SyntaxKind["PackageKeyword"] = 109] = "PackageKeyword";
    SyntaxKind[SyntaxKind["PrivateKeyword"] = 110] = "PrivateKeyword";
    SyntaxKind[SyntaxKind["ProtectedKeyword"] = 111] = "ProtectedKeyword";
    SyntaxKind[SyntaxKind["PublicKeyword"] = 112] = "PublicKeyword";
    SyntaxKind[SyntaxKind["StaticKeyword"] = 113] = "StaticKeyword";
    SyntaxKind[SyntaxKind["YieldKeyword"] = 114] = "YieldKeyword";
    // Contextual keywords
    SyntaxKind[SyntaxKind["AbstractKeyword"] = 115] = "AbstractKeyword";
    SyntaxKind[SyntaxKind["AsKeyword"] = 116] = "AsKeyword";
    SyntaxKind[SyntaxKind["AnyKeyword"] = 117] = "AnyKeyword";
    SyntaxKind[SyntaxKind["AsyncKeyword"] = 118] = "AsyncKeyword";
    SyntaxKind[SyntaxKind["AwaitKeyword"] = 119] = "AwaitKeyword";
    SyntaxKind[SyntaxKind["BooleanKeyword"] = 120] = "BooleanKeyword";
    SyntaxKind[SyntaxKind["ConstructorKeyword"] = 121] = "ConstructorKeyword";
    SyntaxKind[SyntaxKind["DeclareKeyword"] = 122] = "DeclareKeyword";
    SyntaxKind[SyntaxKind["GetKeyword"] = 123] = "GetKeyword";
    SyntaxKind[SyntaxKind["IsKeyword"] = 124] = "IsKeyword";
    SyntaxKind[SyntaxKind["ModuleKeyword"] = 125] = "ModuleKeyword";
    SyntaxKind[SyntaxKind["NamespaceKeyword"] = 126] = "NamespaceKeyword";
    SyntaxKind[SyntaxKind["NeverKeyword"] = 127] = "NeverKeyword";
    SyntaxKind[SyntaxKind["ReadonlyKeyword"] = 128] = "ReadonlyKeyword";
    SyntaxKind[SyntaxKind["RequireKeyword"] = 129] = "RequireKeyword";
    SyntaxKind[SyntaxKind["NumberKeyword"] = 130] = "NumberKeyword";
    SyntaxKind[SyntaxKind["SetKeyword"] = 131] = "SetKeyword";
    SyntaxKind[SyntaxKind["StringKeyword"] = 132] = "StringKeyword";
    SyntaxKind[SyntaxKind["SymbolKeyword"] = 133] = "SymbolKeyword";
    SyntaxKind[SyntaxKind["TypeKeyword"] = 134] = "TypeKeyword";
    SyntaxKind[SyntaxKind["UndefinedKeyword"] = 135] = "UndefinedKeyword";
    SyntaxKind[SyntaxKind["FromKeyword"] = 136] = "FromKeyword";
    SyntaxKind[SyntaxKind["GlobalKeyword"] = 137] = "GlobalKeyword";
    SyntaxKind[SyntaxKind["OfKeyword"] = 138] = "OfKeyword";
    // Parse tree nodes
    // Names
    SyntaxKind[SyntaxKind["QualifiedName"] = 139] = "QualifiedName";
    SyntaxKind[SyntaxKind["ComputedPropertyName"] = 140] = "ComputedPropertyName";
    // Signature elements
    SyntaxKind[SyntaxKind["TypeParameter"] = 141] = "TypeParameter";
    SyntaxKind[SyntaxKind["Parameter"] = 142] = "Parameter";
    SyntaxKind[SyntaxKind["Decorator"] = 143] = "Decorator";
    // TypeMember
    SyntaxKind[SyntaxKind["PropertySignature"] = 144] = "PropertySignature";
    SyntaxKind[SyntaxKind["PropertyDeclaration"] = 145] = "PropertyDeclaration";
    SyntaxKind[SyntaxKind["MethodSignature"] = 146] = "MethodSignature";
    SyntaxKind[SyntaxKind["MethodDeclaration"] = 147] = "MethodDeclaration";
    SyntaxKind[SyntaxKind["Constructor"] = 148] = "Constructor";
    SyntaxKind[SyntaxKind["GetAccessor"] = 149] = "GetAccessor";
    SyntaxKind[SyntaxKind["SetAccessor"] = 150] = "SetAccessor";
    SyntaxKind[SyntaxKind["CallSignature"] = 151] = "CallSignature";
    SyntaxKind[SyntaxKind["ConstructSignature"] = 152] = "ConstructSignature";
    SyntaxKind[SyntaxKind["IndexSignature"] = 153] = "IndexSignature";
    // Type
    SyntaxKind[SyntaxKind["TypePredicate"] = 154] = "TypePredicate";
    SyntaxKind[SyntaxKind["TypeReference"] = 155] = "TypeReference";
    SyntaxKind[SyntaxKind["FunctionType"] = 156] = "FunctionType";
    SyntaxKind[SyntaxKind["ConstructorType"] = 157] = "ConstructorType";
    SyntaxKind[SyntaxKind["TypeQuery"] = 158] = "TypeQuery";
    SyntaxKind[SyntaxKind["TypeLiteral"] = 159] = "TypeLiteral";
    SyntaxKind[SyntaxKind["ArrayType"] = 160] = "ArrayType";
    SyntaxKind[SyntaxKind["TupleType"] = 161] = "TupleType";
    SyntaxKind[SyntaxKind["UnionType"] = 162] = "UnionType";
    SyntaxKind[SyntaxKind["IntersectionType"] = 163] = "IntersectionType";
    SyntaxKind[SyntaxKind["ParenthesizedType"] = 164] = "ParenthesizedType";
    SyntaxKind[SyntaxKind["ThisType"] = 165] = "ThisType";
    SyntaxKind[SyntaxKind["LiteralType"] = 166] = "LiteralType";
    // Binding patterns
    SyntaxKind[SyntaxKind["ObjectBindingPattern"] = 167] = "ObjectBindingPattern";
    SyntaxKind[SyntaxKind["ArrayBindingPattern"] = 168] = "ArrayBindingPattern";
    SyntaxKind[SyntaxKind["BindingElement"] = 169] = "BindingElement";
    // Expression
    SyntaxKind[SyntaxKind["ArrayLiteralExpression"] = 170] = "ArrayLiteralExpression";
    SyntaxKind[SyntaxKind["ObjectLiteralExpression"] = 171] = "ObjectLiteralExpression";
    SyntaxKind[SyntaxKind["PropertyAccessExpression"] = 172] = "PropertyAccessExpression";
    SyntaxKind[SyntaxKind["ElementAccessExpression"] = 173] = "ElementAccessExpression";
    SyntaxKind[SyntaxKind["CallExpression"] = 174] = "CallExpression";
    SyntaxKind[SyntaxKind["NewExpression"] = 175] = "NewExpression";
    SyntaxKind[SyntaxKind["TaggedTemplateExpression"] = 176] = "TaggedTemplateExpression";
    SyntaxKind[SyntaxKind["TypeAssertionExpression"] = 177] = "TypeAssertionExpression";
    SyntaxKind[SyntaxKind["ParenthesizedExpression"] = 178] = "ParenthesizedExpression";
    SyntaxKind[SyntaxKind["FunctionExpression"] = 179] = "FunctionExpression";
    SyntaxKind[SyntaxKind["ArrowFunction"] = 180] = "ArrowFunction";
    SyntaxKind[SyntaxKind["DeleteExpression"] = 181] = "DeleteExpression";
    SyntaxKind[SyntaxKind["TypeOfExpression"] = 182] = "TypeOfExpression";
    SyntaxKind[SyntaxKind["VoidExpression"] = 183] = "VoidExpression";
    SyntaxKind[SyntaxKind["AwaitExpression"] = 184] = "AwaitExpression";
    SyntaxKind[SyntaxKind["PrefixUnaryExpression"] = 185] = "PrefixUnaryExpression";
    SyntaxKind[SyntaxKind["PostfixUnaryExpression"] = 186] = "PostfixUnaryExpression";
    SyntaxKind[SyntaxKind["BinaryExpression"] = 187] = "BinaryExpression";
    SyntaxKind[SyntaxKind["ConditionalExpression"] = 188] = "ConditionalExpression";
    SyntaxKind[SyntaxKind["TemplateExpression"] = 189] = "TemplateExpression";
    SyntaxKind[SyntaxKind["YieldExpression"] = 190] = "YieldExpression";
    SyntaxKind[SyntaxKind["SpreadElementExpression"] = 191] = "SpreadElementExpression";
    SyntaxKind[SyntaxKind["ClassExpression"] = 192] = "ClassExpression";
    SyntaxKind[SyntaxKind["OmittedExpression"] = 193] = "OmittedExpression";
    SyntaxKind[SyntaxKind["ExpressionWithTypeArguments"] = 194] = "ExpressionWithTypeArguments";
    SyntaxKind[SyntaxKind["AsExpression"] = 195] = "AsExpression";
    SyntaxKind[SyntaxKind["NonNullExpression"] = 196] = "NonNullExpression";
    // Misc
    SyntaxKind[SyntaxKind["TemplateSpan"] = 197] = "TemplateSpan";
    SyntaxKind[SyntaxKind["SemicolonClassElement"] = 198] = "SemicolonClassElement";
    // Element
    SyntaxKind[SyntaxKind["Block"] = 199] = "Block";
    SyntaxKind[SyntaxKind["VariableStatement"] = 200] = "VariableStatement";
    SyntaxKind[SyntaxKind["EmptyStatement"] = 201] = "EmptyStatement";
    SyntaxKind[SyntaxKind["ExpressionStatement"] = 202] = "ExpressionStatement";
    SyntaxKind[SyntaxKind["IfStatement"] = 203] = "IfStatement";
    SyntaxKind[SyntaxKind["DoStatement"] = 204] = "DoStatement";
    SyntaxKind[SyntaxKind["WhileStatement"] = 205] = "WhileStatement";
    SyntaxKind[SyntaxKind["ForStatement"] = 206] = "ForStatement";
    SyntaxKind[SyntaxKind["ForInStatement"] = 207] = "ForInStatement";
    SyntaxKind[SyntaxKind["ForOfStatement"] = 208] = "ForOfStatement";
    SyntaxKind[SyntaxKind["ContinueStatement"] = 209] = "ContinueStatement";
    SyntaxKind[SyntaxKind["BreakStatement"] = 210] = "BreakStatement";
    SyntaxKind[SyntaxKind["ReturnStatement"] = 211] = "ReturnStatement";
    SyntaxKind[SyntaxKind["WithStatement"] = 212] = "WithStatement";
    SyntaxKind[SyntaxKind["SwitchStatement"] = 213] = "SwitchStatement";
    SyntaxKind[SyntaxKind["LabeledStatement"] = 214] = "LabeledStatement";
    SyntaxKind[SyntaxKind["ThrowStatement"] = 215] = "ThrowStatement";
    SyntaxKind[SyntaxKind["TryStatement"] = 216] = "TryStatement";
    SyntaxKind[SyntaxKind["DebuggerStatement"] = 217] = "DebuggerStatement";
    SyntaxKind[SyntaxKind["VariableDeclaration"] = 218] = "VariableDeclaration";
    SyntaxKind[SyntaxKind["VariableDeclarationList"] = 219] = "VariableDeclarationList";
    SyntaxKind[SyntaxKind["FunctionDeclaration"] = 220] = "FunctionDeclaration";
    SyntaxKind[SyntaxKind["ClassDeclaration"] = 221] = "ClassDeclaration";
    SyntaxKind[SyntaxKind["InterfaceDeclaration"] = 222] = "InterfaceDeclaration";
    SyntaxKind[SyntaxKind["TypeAliasDeclaration"] = 223] = "TypeAliasDeclaration";
    SyntaxKind[SyntaxKind["EnumDeclaration"] = 224] = "EnumDeclaration";
    SyntaxKind[SyntaxKind["ModuleDeclaration"] = 225] = "ModuleDeclaration";
    SyntaxKind[SyntaxKind["ModuleBlock"] = 226] = "ModuleBlock";
    SyntaxKind[SyntaxKind["CaseBlock"] = 227] = "CaseBlock";
    SyntaxKind[SyntaxKind["NamespaceExportDeclaration"] = 228] = "NamespaceExportDeclaration";
    SyntaxKind[SyntaxKind["ImportEqualsDeclaration"] = 229] = "ImportEqualsDeclaration";
    SyntaxKind[SyntaxKind["ImportDeclaration"] = 230] = "ImportDeclaration";
    SyntaxKind[SyntaxKind["ImportClause"] = 231] = "ImportClause";
    SyntaxKind[SyntaxKind["NamespaceImport"] = 232] = "NamespaceImport";
    SyntaxKind[SyntaxKind["NamedImports"] = 233] = "NamedImports";
    SyntaxKind[SyntaxKind["ImportSpecifier"] = 234] = "ImportSpecifier";
    SyntaxKind[SyntaxKind["ExportAssignment"] = 235] = "ExportAssignment";
    SyntaxKind[SyntaxKind["ExportDeclaration"] = 236] = "ExportDeclaration";
    SyntaxKind[SyntaxKind["NamedExports"] = 237] = "NamedExports";
    SyntaxKind[SyntaxKind["ExportSpecifier"] = 238] = "ExportSpecifier";
    SyntaxKind[SyntaxKind["MissingDeclaration"] = 239] = "MissingDeclaration";
    // Module references
    SyntaxKind[SyntaxKind["ExternalModuleReference"] = 240] = "ExternalModuleReference";
    // JSX
    SyntaxKind[SyntaxKind["JsxElement"] = 241] = "JsxElement";
    SyntaxKind[SyntaxKind["JsxSelfClosingElement"] = 242] = "JsxSelfClosingElement";
    SyntaxKind[SyntaxKind["JsxOpeningElement"] = 243] = "JsxOpeningElement";
    SyntaxKind[SyntaxKind["JsxText"] = 244] = "JsxText";
    SyntaxKind[SyntaxKind["JsxClosingElement"] = 245] = "JsxClosingElement";
    SyntaxKind[SyntaxKind["JsxAttribute"] = 246] = "JsxAttribute";
    SyntaxKind[SyntaxKind["JsxSpreadAttribute"] = 247] = "JsxSpreadAttribute";
    SyntaxKind[SyntaxKind["JsxExpression"] = 248] = "JsxExpression";
    // Clauses
    SyntaxKind[SyntaxKind["CaseClause"] = 249] = "CaseClause";
    SyntaxKind[SyntaxKind["DefaultClause"] = 250] = "DefaultClause";
    SyntaxKind[SyntaxKind["HeritageClause"] = 251] = "HeritageClause";
    SyntaxKind[SyntaxKind["CatchClause"] = 252] = "CatchClause";
    // Property assignments
    SyntaxKind[SyntaxKind["PropertyAssignment"] = 253] = "PropertyAssignment";
    SyntaxKind[SyntaxKind["ShorthandPropertyAssignment"] = 254] = "ShorthandPropertyAssignment";
    // Enum
    SyntaxKind[SyntaxKind["EnumMember"] = 255] = "EnumMember";
    // Top-level nodes
    SyntaxKind[SyntaxKind["SourceFile"] = 256] = "SourceFile";
    // JSDoc nodes
    SyntaxKind[SyntaxKind["JSDocTypeExpression"] = 257] = "JSDocTypeExpression";
    // The * type
    SyntaxKind[SyntaxKind["JSDocAllType"] = 258] = "JSDocAllType";
    // The ? type
    SyntaxKind[SyntaxKind["JSDocUnknownType"] = 259] = "JSDocUnknownType";
    SyntaxKind[SyntaxKind["JSDocArrayType"] = 260] = "JSDocArrayType";
    SyntaxKind[SyntaxKind["JSDocUnionType"] = 261] = "JSDocUnionType";
    SyntaxKind[SyntaxKind["JSDocTupleType"] = 262] = "JSDocTupleType";
    SyntaxKind[SyntaxKind["JSDocNullableType"] = 263] = "JSDocNullableType";
    SyntaxKind[SyntaxKind["JSDocNonNullableType"] = 264] = "JSDocNonNullableType";
    SyntaxKind[SyntaxKind["JSDocRecordType"] = 265] = "JSDocRecordType";
    SyntaxKind[SyntaxKind["JSDocRecordMember"] = 266] = "JSDocRecordMember";
    SyntaxKind[SyntaxKind["JSDocTypeReference"] = 267] = "JSDocTypeReference";
    SyntaxKind[SyntaxKind["JSDocOptionalType"] = 268] = "JSDocOptionalType";
    SyntaxKind[SyntaxKind["JSDocFunctionType"] = 269] = "JSDocFunctionType";
    SyntaxKind[SyntaxKind["JSDocVariadicType"] = 270] = "JSDocVariadicType";
    SyntaxKind[SyntaxKind["JSDocConstructorType"] = 271] = "JSDocConstructorType";
    SyntaxKind[SyntaxKind["JSDocThisType"] = 272] = "JSDocThisType";
    SyntaxKind[SyntaxKind["JSDocComment"] = 273] = "JSDocComment";
    SyntaxKind[SyntaxKind["JSDocTag"] = 274] = "JSDocTag";
    SyntaxKind[SyntaxKind["JSDocParameterTag"] = 275] = "JSDocParameterTag";
    SyntaxKind[SyntaxKind["JSDocReturnTag"] = 276] = "JSDocReturnTag";
    SyntaxKind[SyntaxKind["JSDocTypeTag"] = 277] = "JSDocTypeTag";
    SyntaxKind[SyntaxKind["JSDocTemplateTag"] = 278] = "JSDocTemplateTag";
    SyntaxKind[SyntaxKind["JSDocTypedefTag"] = 279] = "JSDocTypedefTag";
    SyntaxKind[SyntaxKind["JSDocPropertyTag"] = 280] = "JSDocPropertyTag";
    SyntaxKind[SyntaxKind["JSDocTypeLiteral"] = 281] = "JSDocTypeLiteral";
    SyntaxKind[SyntaxKind["JSDocLiteralType"] = 282] = "JSDocLiteralType";
    SyntaxKind[SyntaxKind["JSDocNullKeyword"] = 283] = "JSDocNullKeyword";
    SyntaxKind[SyntaxKind["JSDocUndefinedKeyword"] = 284] = "JSDocUndefinedKeyword";
    SyntaxKind[SyntaxKind["JSDocNeverKeyword"] = 285] = "JSDocNeverKeyword";
    // Synthesized list
    SyntaxKind[SyntaxKind["SyntaxList"] = 286] = "SyntaxList";
    // Enum value count
    SyntaxKind[SyntaxKind["Count"] = 287] = "Count";
    // Markers
    SyntaxKind[SyntaxKind["FirstAssignment"] = 56] = "FirstAssignment";
    SyntaxKind[SyntaxKind["LastAssignment"] = 68] = "LastAssignment";
    SyntaxKind[SyntaxKind["FirstReservedWord"] = 70] = "FirstReservedWord";
    SyntaxKind[SyntaxKind["LastReservedWord"] = 105] = "LastReservedWord";
    SyntaxKind[SyntaxKind["FirstKeyword"] = 70] = "FirstKeyword";
    SyntaxKind[SyntaxKind["LastKeyword"] = 138] = "LastKeyword";
    SyntaxKind[SyntaxKind["FirstFutureReservedWord"] = 106] = "FirstFutureReservedWord";
    SyntaxKind[SyntaxKind["LastFutureReservedWord"] = 114] = "LastFutureReservedWord";
    SyntaxKind[SyntaxKind["FirstTypeNode"] = 154] = "FirstTypeNode";
    SyntaxKind[SyntaxKind["LastTypeNode"] = 166] = "LastTypeNode";
    SyntaxKind[SyntaxKind["FirstPunctuation"] = 15] = "FirstPunctuation";
    SyntaxKind[SyntaxKind["LastPunctuation"] = 68] = "LastPunctuation";
    SyntaxKind[SyntaxKind["FirstToken"] = 0] = "FirstToken";
    SyntaxKind[SyntaxKind["LastToken"] = 138] = "LastToken";
    SyntaxKind[SyntaxKind["FirstTriviaToken"] = 2] = "FirstTriviaToken";
    SyntaxKind[SyntaxKind["LastTriviaToken"] = 7] = "LastTriviaToken";
    SyntaxKind[SyntaxKind["FirstLiteralToken"] = 8] = "FirstLiteralToken";
    SyntaxKind[SyntaxKind["LastLiteralToken"] = 11] = "LastLiteralToken";
    SyntaxKind[SyntaxKind["FirstTemplateToken"] = 11] = "FirstTemplateToken";
    SyntaxKind[SyntaxKind["LastTemplateToken"] = 14] = "LastTemplateToken";
    SyntaxKind[SyntaxKind["FirstBinaryOperator"] = 25] = "FirstBinaryOperator";
    SyntaxKind[SyntaxKind["LastBinaryOperator"] = 68] = "LastBinaryOperator";
    SyntaxKind[SyntaxKind["FirstNode"] = 139] = "FirstNode";
    SyntaxKind[SyntaxKind["FirstJSDocNode"] = 257] = "FirstJSDocNode";
    SyntaxKind[SyntaxKind["LastJSDocNode"] = 282] = "LastJSDocNode";
    SyntaxKind[SyntaxKind["FirstJSDocTagNode"] = 273] = "FirstJSDocTagNode";
    SyntaxKind[SyntaxKind["LastJSDocTagNode"] = 285] = "LastJSDocTagNode";
}
exports.init = init;
