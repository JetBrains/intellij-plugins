main() {
  var result = new Result();
  if (<caret>) {
    var data = findData();
    var p = new Processor();
    p.process(data);
    result += p.getResult();
  } else {

  }
}