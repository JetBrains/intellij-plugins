int test(Map<String, List<String>> map){
  int sum = 0;
  for(var str in map.values()) {
    sum += str.len<caret>gth;
  }
  return sum;
}