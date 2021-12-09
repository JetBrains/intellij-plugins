// no_typo <TYPO descr="Typo: In word 'typoo'">typoo</TYPO> CorrectSpelling <TYPO descr="Typo: In word 'Baad'">Baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>

var correctSpelling1;
var <TYPO descr="Typo: In word 'baad'">baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>;

class CorrectSpelling{}
class <TYPO descr="Typo: In word 'Baad'">Baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>1{}

void correctSpelling2(){}
void <TYPO descr="Typo: In word 'baad'">baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>2(){}

var correctEmptyStr ='';
var wrongEmpty<TYPO descr="Typo: In word 'Strrr'">Strrr</TYPO> = '';

var correctSingleQuotes = 'Spelling';
var wrongSingleQuotes = '<TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>';

var correctDoubleQuotes = "Spelling";
var wrongDoubleQuotes = "<TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>";

var correctEmbeddingWithDictionary = "Embedder" + "dartdevc";
var wrongEmbeddingEvenWithDictionary = "<TYPO descr="Typo: In word 'Embeder'">Embeder</TYPO>" + "<TYPO descr="Typo: In word 'dartdevcompiler'">dartdevcompiler</TYPO>";

var correctMultiLine = '''
Good
Spelling
''';
var wrongMultiLine = '''
<TYPO descr="Typo: In word 'Baad'">Baad</TYPO>
<TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>
''';

var correctNestedDoubleQuotes = 'good "good"';
var wrongNestedDoubleQuotes = 'good "<TYPO descr="Typo: In word 'baad'">baad</TYPO>"';
var wrongNestedDoubleQuotesTwoTypos = '<TYPO descr="Typo: In word 'baad'">baad</TYPO> "<TYPO descr="Typo: In word 'baad'">baad</TYPO>"';

var correctNestedSingleQuotes = "good 'good'";
var wrongNestedSingleQuotes = "good '<TYPO descr="Typo: In word 'baad'">baad</TYPO>'";
var wrongNestedSingleQuotesTwoTypos = "<TYPO descr="Typo: In word 'baad'">baad</TYPO> '<TYPO descr="Typo: In word 'baad'">baad</TYPO>'";

var correctSimpleInterpolation = '$correctEmptyStr,good $correctEmptyStr good$correctEmptyStr';
var wrongSimpleInterpolation = '$wrongEmptyStrrr,<TYPO descr="Typo: In word 'baad'">baad</TYPO> $wrongEmptyStrrr <TYPO descr="Typo: In word 'baad'">baad</TYPO>$wrongEmptyStrrr';

var correctComplexInterpolation = '${wrongEmptyStrrr.toString()},good ${wrongEmptyStrrr.toString()} good${wrongEmptyStrrr.toString()}';
var wrongComplexInterpolation = '${wrongEmptyStrrr.toString()},<TYPO descr="Typo: In word 'baad'">baad</TYPO> ${wrongEmptyStrrr.toString()} <TYPO descr="Typo: In word 'baad'">baad</TYPO>${wrongEmptyStrrr.toString()}';

var correctEscapeXHexDigits = '\x0Agood \x08good\x0D';
var wrongEscapeXHexDigits = '\x0A<TYPO descr="Typo: In word 'baad'">baad</TYPO> \x08<TYPO descr="Typo: In word 'baad'">baad</TYPO>\x0D';

var correctEscapeUHexDigits = '\uaabbgood \uaabbgood\uaabb';
var wrongEscapeUHexDigits = '\uaabb<TYPO descr="Typo: In word 'baad'">baad</TYPO> \uaabb<TYPO descr="Typo: In word 'baad'">baad</TYPO>\uaabb';

var correctEscapeUHexDigitSequence = '\u{a}good \u{aa}good\u{aaa}good\u{aaaa}\u{aaaaa}good';
var wrongEscapeUHexDigitSequence = '\u{a}<TYPO descr="Typo: In word 'baad'">baad</TYPO> \u{aa}<TYPO descr="Typo: In word 'baad'">baad</TYPO>\u{aaa}<TYPO descr="Typo: In word 'baad'">baad</TYPO>\u{aaaa}\u{aaaaa}<TYPO descr="Typo: In word 'baad'">baad</TYPO>';

var correctInterpolationWithEscapeSequence = '$correctEmptyStr,good ${correctEmptyStr.toString()} good\ngood \uaabbgood';
var wrongInterpolationWithEscapeSequence = '$wrongEmptyStrrr,<TYPO descr="Typo: In word 'baad'">baad</TYPO> ${wrongEmptyStrrr.toString()} <TYPO descr="Typo: In word 'baad'">baad</TYPO>\n<TYPO descr="Typo: In word 'baad'">baad</TYPO> \uaabb<TYPO descr="Typo: In word 'baad'">baad</TYPO>';

var correctEscapeWithSimpleInterpolation = 'good\\$correctEmptyStr\\$correctEmptyStr good';
var wrongEscapeWithSimpleInterpolation = '<TYPO descr="Typo: In word 'baad'">baad</TYPO>\\$wrongEmptyStrrr\\$wrongEmptyStrrr <TYPO descr="Typo: In word 'baad'">baad</TYPO>';

var correctEscapeWithComplexInterpolation = 'good\\${correctEmptyStr.toString()}\\${correctEmptyStr.toString()} good';
var wrongEscapeWithComplexInterpolation = '<TYPO descr="Typo: In word 'baad'">baad</TYPO>\\${wrongEmptyStrrr.toString()}\\${wrongEmptyStrrr.toString()} <TYPO descr="Typo: In word 'baad'">baad</TYPO>';

var correctDollarEscape = '\$good good \$good\$';
var wrongDollarEscape = '\$<TYPO descr="Typo: In word 'baad'">baad</TYPO> <TYPO descr="Typo: In word 'baad'">baad</TYPO> \$<TYPO descr="Typo: In word 'baad'">baad</TYPO>\$';
