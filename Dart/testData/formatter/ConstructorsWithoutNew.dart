class SimpleObject {
  final String aString;
  final int anInt;
  final double aDouble;
  final List<String> aListOfStrings;
  final List<int> aListOfInts;
  final List<double> aListOfDoubles;

  SimpleObject.fromJson(Map<String, dynamic> json)
      : aString = json['aString'],
        anInt = json['anInt'] ?? 0,
        aDouble = json['aDouble'] ?? 0.0,
        aListOfStrings = List<String>.from(json['aListOfStrings']),
  aListOfInts = List<int>.from(json['aListOfInts']),
  aListOfDoubles = List<double>.from(json['aListOfDoubles']);
}
