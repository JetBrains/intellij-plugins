main(){
  var strings = new List<String>();
  int count = 0;
  for(str in strings) {
    count += str.len<caret>gth;
  }
}