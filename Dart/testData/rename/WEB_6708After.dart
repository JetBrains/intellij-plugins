class Bar {
  String hello(msg, to, [renamed, rate]) => '${renamed} sent ${msg} to ${to} via ${rate}';
}

void main() {
  var barr = new Bar();
  barr.hello('world', 'Seth', rate:'First Class', renamed:'Bob');
}