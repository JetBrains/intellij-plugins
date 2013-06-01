import 'DART_UNITTEST';
import 'TEST_FILE_PATH' as testPrefix;

main() {
  var config = new JetBrainsUnitConfig();
  unittestConfiguration = config;
  testPrefix.main();
}

class JetBrainsUnitConfig extends Configuration {
  String name = 'NAME';
  String scope = 'SCOPE';
  int maxId = 0;
  Map<String, int> group2id;
  bool done = false;

  void onStart() {
    _filterTests(testCases);
    testCases.forEach((TestCase testCase) => maxId = maxId < testCase.id ? testCase.id : maxId);
    maxId += 2;
    group2id = {'': 0};
    printTCMessage('enteredTheMatrix', {});
    _createGroups();
    testCases.forEach((TestCase testCase){
      printTCMessage('testStarted', {
          'name' : getName(testCase),
          'parentNodeId' : group2id[testCase.currentGroup],
          'nodeId' : (testCase.id + 1),
          'nodeType' : 'test'
      });
    });
  }

  String getName(TestCase testCase) {
    if(testCase.currentGroup == '') return testCase.description;
    return testCase.description.substring(testCase.currentGroup.length + 1);
  }

  _createGroups() {
    String lastGroup = '';
    Set<String> groupsSet = new Set.from(testCases.map((TestCase testCase) => testCase.currentGroup));
    List<String> groups = new List.from(groupsSet);
    groups.sort((String a, String b) => a.length - b.length);
    groups.removeWhere((String groupName) => groupName == '');

    for(int i = 0; i < groups.length; ++i) {
      var parentGroup = '';
      for(int j = i-1; j >= 0; --j) {
        if(groups[i].startsWith(groups[j])) {
          parentGroup = groups[j];
          break;
        }
      }
      var groupName = groups[i];
      if(group2id.containsKey(groupName)) continue;
      var nodeId = ++maxId;
      group2id[groupName] = nodeId;
      if(parentGroup != '') {
        groupName = groupName.substring(parentGroup.length + 1);
      }
      printTCMessage('testSuiteStarted', {'name' : groupName, 'parentNodeId' : group2id[parentGroup], 'nodeId' : nodeId, 'nodeType' : 'test'});
    }
  }

  void onSummary(int passed, int failed, int errors, List<TestCase> results, String uncaughtError) {
    if(done) return;
    done = true;
    List<int> ids = new List.from(group2id.values);
    ids.sort((int a, int b) => b-a);
    for(int id in ids){
      if (id > 0) {
        printTCMessage('testSuiteFinished', {'nodeId' : id});
      }
    };
  }

  _filterTests(List<TestCase> tests) {
    if (name == null) {
      return;
    }

    if (scope == 'GROUP') {
      filterTests((TestCase testCase) => testCase.currentGroup.contains(name));
    }
    else if (scope == 'METHOD') {
      filterTests((TestCase testCase) => getName(testCase) == name);
    }
  }

  _filter(List<TestCase> cases, bool condition(TestCase)) {
    int i = 0;
    while (i < testCases.length) {
      if (condition(testCases[i])) {
        ++i;
        continue;
      }
      testCases.removeRange(i, 1);
    }
  }

  void onTestResult(TestCase testCase) {
    String messageName = 'internalError';
    switch (testCase.result) {
      case 'pass': messageName = 'testFinished'; break;
      case 'fail': messageName = 'testFailed'; break;
    }
    printTCMessage(messageName, {
        'nodeId' : (testCase.id + 1),
        'message': '${testCase.message}\n${testCase.stackTrace}'
    });
  }

  printTCMessage(String messageName, Map attrs) {
    var out = new StringBuffer();
    out.write("##teamcity[$messageName");
    attrs.forEach((key, value){
      out.write(" $key='${escapseString(value.toString())}'");
    });
    out.write("]");
    print(out.toString());
  }

  String escapseString(String str){
    var out = new StringBuffer();
    for(var ch in str.split("")){
      var current = escapseChar(ch);
      out.write(current == 0 ? ch : '|$current');
    }
    return out.toString();
  }

  escapseChar(ch) {
    switch (ch) {
      case '\n': return 'n';
      case '\r': return 'r';
      case '\u0085': return 'x'; // next-line character
      case '\u2028': return 'l'; // line-separator character
      case '\u2029': return 'p'; // paragraph-separator character
      case '|': return '|';
      case '\'': return '\'';
      case '[': return '[';
      case ']': return ']';
      default: return 0;
    }
  }
}