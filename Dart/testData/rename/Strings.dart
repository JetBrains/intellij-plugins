var s1 = "Hello";
var s3 = 'world';
var s4 = "$s<caret>1, ${s3 + "!"}";
var s5 = '$s1, ${s3 + '!'}';
var s6 = """
$s1, ${s3 + "!"}
""";
var s7 = '''
$s1, ${s3 + '!'}
''';
