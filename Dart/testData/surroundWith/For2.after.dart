main() {
  var result = new Result();
  for (<caret>) {
    var data = findData();
    var p = new Processor();
    p.process(data);
    result += p.getResult();
  }
}