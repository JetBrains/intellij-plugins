void main() {
  String? presentKey = 'Apple';
  String? absentKey;

  int? presentValue = 3;
  int? absentValue;
  int? absentValue2;

  var items = [1, ?absentValue, ?absentValue2 ?? foo() ?? bar(), 5,];

  var itemsA = {presentKey: absentValue}; // {Apple: null}
  var itemsB = {presentKey: ?absentValue}; // {}

  var itemsC = {absentKey: presentValue}; // {null: 3}
  var itemsD = {?absentKey: presentValue}; // {}

  var itemsE = {absentKey: absentValue}; // {null: null}
  var itemsF = {?absentKey: ?absentValue}; // {}

  var itemsG = {?absentValue2 ?? foo(): ?absentValue}; // {}
}

String? foo() {
  return null;
}

String? bar() {
  return null;
}