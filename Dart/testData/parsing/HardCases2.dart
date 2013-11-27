part "frog/natives.js";

var lives = new Set<HInstruction>.from(other.lives);

int getOne() native r""" return 1;""";
int getTwo() native """ return 2;""";
int getThree() native r" return 3;";
int getFour() native " return 4;";


int getOne() native r''' return 1;''';
int getTwo() native ''' return 2;''';
int getThree() native r' return 3;';
int getFour() native ' return 4;';

var a = '''
this is legal string with 'quoted' ''word''
''';

var b = r'''
this is legal raw string with 'quoted' ''word''
''';

var c = """
this is legal string with "quoted" ""word""
""";

var d = r"""
this is legal raw string with "quoted" ""word""
""";

class Foo {
    Foo.from(int type) : this(type > 0);
}

main(){
  if (obj is Map<String, Dynamic>) {
    print("JSON");
  }
  if (obj is! Map<String, Dynamic>) {
    print("JSON");
  }

  do ++i;
  while (process(i));

  Set set = new Set();

  LibraryElement element = new LibraryElement(script);
  native.maybeEnableNative(compiler, element, uri);

  if (isFinal && _computedValue.isConst) {
    ; // keep const as is here
  }

  Token operator = token;
  token = token.next;


  var foo = new Foo()..bar = 239
                     ..baz.getBar().getBaz();
}
