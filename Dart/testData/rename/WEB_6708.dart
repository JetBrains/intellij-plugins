class Bar {
  String hello(msg, to, [fr<caret>om, rate]) => '${from} sent ${msg} to ${to} via ${rate}';
}

void main() {
  var barr = new Bar();
  barr.hello('world', 'Seth', rate:'First Class', from:'Bob');
}