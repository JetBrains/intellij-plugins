num extracted(int i) {
  var j = i + 239;
  j = j - 1;
  return j;
}

main() {
  var i = 0;
  var j = extracted(i);
  return i + j;
}