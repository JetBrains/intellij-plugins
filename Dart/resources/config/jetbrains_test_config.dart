import 'dart:collection';

import 'DART_UNITTEST';
import 'TEST_FILE_URI' as testPrefix;

typedef F1(args);

main(List<String> args) {
  var config = new JetBrainsUnitConfig();
  unittestConfiguration = config;
  if (testPrefix.main is F1) {
    testPrefix.main(args);
  } else {
    testPrefix.main();
  }
}
