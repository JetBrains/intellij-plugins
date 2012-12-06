main() {
  var result = new Result();
  do {
    var data = findData();
    var p = new Processor();
    p.process(data);
    result += p.getResult();
  } while (<caret>);
}