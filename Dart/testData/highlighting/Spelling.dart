// no_typo <TYPO descr="Typo: In word 'typoo'">typoo</TYPO> CorrectSpelling <TYPO descr="Typo: In word 'Baad'">Baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>

var correctSpelling1;
var <TYPO descr="Typo: In word 'baad'">baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>;

class CorrectSpelling{}
class <TYPO descr="Typo: In word 'Baad'">Baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>1{}

void correctSpelling2(){}
void <TYPO descr="Typo: In word 'baad'">baad</TYPO><TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>2(){}

var emptyStr ='';

var correctSingleQuotes = 'Spelling';
var wrongSingleQuotes = '<TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>';

var correctDoubleQuotes = "Spelling";
var wrongDoubleQuotes = "<TYPO descr="Typo: In word 'Sspelling'">Sspelling</TYPO>";

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
