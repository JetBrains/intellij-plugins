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
var s5r = @'''
long string with $"text"
''';
var s6r = @"""
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
