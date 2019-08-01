foo({String str1= '1', String str2= '2'}) { }

main() {
  foo(str<caret>2: '', str1: '');
}
