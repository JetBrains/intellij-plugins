main1() {
  throw 0;
}

main2() {
  (true) ? print(42) : throw new ArgumentError();
}
