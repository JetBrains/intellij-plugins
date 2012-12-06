int getLength(List<String> strings) {
  int result = '';
  for(int i = 0; i < strings.length; ++i) {
    result += strings[i].len<caret>gth;;
  }
  return result;
}