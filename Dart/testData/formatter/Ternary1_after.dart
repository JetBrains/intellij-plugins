m1() {
  var kind = element != null
      ? arg
      : secondArgumentThatIsReallyLong;
}

m2() {
  var kind = element != null
      ? reallyLongArgument
      : arg;
}

m3() {
  return x ? 0 : 1;
}

n(x) => x ? 0 : 1;
