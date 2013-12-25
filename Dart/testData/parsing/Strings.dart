var a = "not closed
var a = 'not closed
var a = "not closed\
var a = 'not closed\
var a = "not closed \xFF\uFFFF\u{FFFFFF}\"
var a = 'not closed \xFF\uFFFF\u{FFFFFF}\'
var a = '\'';
var a = "\"";
var raw = r'''${\}\
$\''''''''\$\
''';
var raw = r"""${\}$\
\""""""""\$\
""";
var not_closed = "${\
}
var s1 = "Hello";
var s2 = "This is 'sparta'!";
var s3 = 'world';
var s4 = 'This is "sparta"!';
var s5 = '''
long string with "text"
''';
var s6 = """
long string with 'text'
""";
var s5r = r'''
long string with $"text"
''';
var s6r = r"""
long string with $'text'
""";
var s7 = "$s1, ${s3 + "!"}";
var s8 = '$s1, ${s3 + '!'}';
var s7 = """
$s1, ${s3 + "!"}
""";
var s8 = '''
$s1, ${s3 + '!'}
''';
var s9 = '\