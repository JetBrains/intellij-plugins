var tuple = ("first", 2, true);
var record = (number: 123, name: "Main", type: "Street");
var record = (1, a: 2, 3, b: 4);

main() {
  var record = (1, a: 2, 3, b: 4);
  clientParams.forEach((k, v) => log.info()); // no records
  foo.catchError((ex, st) {}).then((List<Row> rows) {}); // no records
  print((1, 2, 3).toString()); // "(1, 2, 3)".
  print((a: 'str', 'int').toString()); // "(a: str, int)".

}

var number = (1); // The number 1.
var record = (1, ); // A record containing the number 1.
var t = (int, String);
var x = (a: say(1), b: say(2));