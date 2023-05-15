/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var intellijParameters = require('./karma-intellij-parameters')
  , intellijUtil = require('./intellijUtil.js')
  , util = require('util')
  , Tree = require('./tree.js');

function getOrCreateBrowserNode(tree, browser) {
  var configFileNode = tree.configFileNode;
  var browserNode = configFileNode.findChildNodeByKey(browser.id);
  if (!browserNode) {
    browserNode = configFileNode.addChildWithKey(browser.id, browser.name, true, 'browser', null);
    browserNode.writeStartMessage();
  }
  return browserNode;
}

function addBrowserErrorNode(tree, browser, error) {
  var browserNode = getOrCreateBrowserNode(tree, browser);
  var browserErrorNode = browserNode.addChild('Error', false, 'browserError', null);
  browserErrorNode.writeStartMessage();
  if (intellijUtil.isString(error)) {
    error = {stack: error};
  }
  const normalizedError = normalizeAssertionError(error.stack, error);
  browserErrorNode.setStatus(3, null, error.message || '', error.stack, null, null);
  browserErrorNode.writeFinishMessage();
}

function getOrCreateLowerSuiteNode(browserNode, suiteNames, write) {
  var node = browserNode
    , len = suiteNames.length;
  for (var i = 0; i < len; i++) {
    var suiteName = suiteNames[i];
    if (suiteName == null) {
      suiteNames.splice(i, 1);
      var message = "[Karma bug found] Suite name is null. Please file an issue in the https://github.com/karma-runner/karma/issues";
      console.error(message);
      write(message + '\n');
      continue;
    }
    var nextNode = node.findChildNodeByKey(suiteName);
    if (!nextNode) {
      var locationHint = intellijUtil.joinList(suiteNames, 0, i + 1, '.');
      nextNode = node.addChild(suiteName, true, 'suite', locationHint);
      nextNode.writeStartMessage();
    }
    node = nextNode;
  }
  return node;
}

function createSpecNode(suiteNode, suiteNames, specName) {
  var names = suiteNames.slice();
  names.push(specName);
  var locationHint = intellijUtil.joinList(names, 0, names.length, '.');
  var specNode = suiteNode.addChild(specName, false, 'test', locationHint);
  specNode.writeStartMessage();
  return specNode;
}

// Makes sure that only intellijReporter is allowed to output.
// Otherwise we'll get messages doubling and possible prefixing '##[teamcity ]' with other output that results
// in test output tree breaking.
// Would be nice to remove this hack.
function clearOtherAdapters(injector, intellijReporter) {
  process.nextTick(function () {
    var multiReporter;
    try {
      multiReporter = injector.get('reporter');
    }
    catch (ex) {
      console.warn("Can't get 'reporter'");
      return;
    }

    if (multiReporter != null) {
      var savedAdapters = intellijReporter.adapters.slice();
      intellijReporter.adapters.forEach(function (adapter) {
        if (typeof multiReporter.removeAdapter === 'function') {
          multiReporter.removeAdapter(adapter);
        }
      });
      if (intellijReporter.adapters.length === 0) {
        Array.prototype.push.apply(intellijReporter.adapters, savedAdapters);
      }
    }
  });
}

function LogManager() {
  this.postponedLog = null;
}

LogManager.prototype.postponeLog = function (log) {
  if (this.postponedLog == null) {
    this.postponedLog = log;
  }
  else {
    this.postponedLog = this.postponedLog + '\n' + log;
  }
};

LogManager.prototype.attachTo = function (specNode) {
  if (this.postponedLog != null) {
    var tree = specNode.tree;
    tree.write('##teamcity[testStdOut nodeId=\'' + specNode.id + '\' out=\'' + intellijUtil.attributeValueEscape(this.postponedLog + '\n') + '\']\n');
    this.postponedLog = null;
  }
};

LogManager.prototype.attachToAnything = function (tree) {
  if (this.postponedLog != null) {
    tree.write(this.postponedLog + '\n');
    this.postponedLog = null;
  }
};

function filterSuiteNames(suiteNames) {
  if (suiteNames.length > 0 && 'Jasmine__TopLevel__Suite' === suiteNames[0]) {
    suiteNames = suiteNames.slice(1);
  }
  return suiteNames;
}

function IntellijReporter(config, fileList, formatError, globalEmitter, injector) {
  require('./kjhtml/kjhtml-specFilter-patch').apply(config.files);
  require('./karma-browser-tracker').startBrowserTracking(globalEmitter);
  require('./karma-intellij-debug').configureTimeouts(injector);
  var logManager = new LogManager();
  this.adapters = [];

  var that = this;
  var write = function (msg) {
    that.adapters.forEach(function(adapter) {
      adapter(msg);
    });
  };

  var tree;
  var beforeRunStart = true;

  this.onRunStart = function (browsers) {
    clearOtherAdapters(injector, that);

    beforeRunStart = false;
    tree = new Tree(intellijParameters.getUserConfigFilePath(), write);
    process.nextTick(function() {
      tree.write('##teamcity[enteredTheMatrix]\n');
    });
  };

  this.onBrowserError = function (browser, error) {
    if (tree == null) {
      var disconnected = browser && typeof browser.state === 'number'
                         && browser.constructor && browser.state === browser.constructor.STATE_DISCONNECTED;
      // skip logging disconnected events (https://github.com/karma-runner/karma/issues/2853)
      if (beforeRunStart || !disconnected) {
        console.error(beforeRunStart ? '"onBrowserError" before "onRunStart"'
                                     : '"onBrowserError" after "onRunComplete"', error);
      }
    }
    else {
      addBrowserErrorNode(tree, browser, error);
    }
  };

  this.onBrowserLog = function (browser, log, type) {
    if (!intellijUtil.isString(log)) {
      log = util.inspect(log, false, null, false);
    }
    logManager.postponeLog(log);
  };

  this.onSpecComplete = function (browser, result) {
    if (result.skipped) {
      // the test is either pending or disabled
      if (!result.pending) {
        return;
      }
    }
    var suiteNames = filterSuiteNames(result.suite)
      , specName = result.description;
    if (specName == null) {
      return;
    }
    if (tree == null) {
      // workaround for https://github.com/karma-runner/karma/issues/1292
      process.stdout.write('Test "' + suiteNames.concat(specName).join('.') + '" skipped\n');
      return;
    }
    var browserNode = getOrCreateBrowserNode(tree, browser);
    var suiteNode = getOrCreateLowerSuiteNode(browserNode, suiteNames, write);
    var specNode = createSpecNode(suiteNode, suiteNames, specName);
    var status = result.pending ? 1 : result.success ? 0 : 2;
    var stack = createStack(result.log, formatError);
    if (stack.length === 0 && result.pending) {
      stack = 'Pending test \'' + specName + '\''
    }
    var firstAssertionError = Array.isArray(result.assertionErrors) ? result.assertionErrors[0] : null;
    var assertionErrorObj = normalizeAssertionError(stack, firstAssertionError);
    specNode.setStatus(status, result.time, assertionErrorObj.message || '', assertionErrorObj.stack,
      assertionErrorObj.expected, assertionErrorObj.actual);
    logManager.attachTo(specNode);
    specNode.writeFinishMessage();
  };

  this.onRunComplete = function (browsers, results) {
    logManager.attachToAnything(tree);
    if (tree != null) {
      tree.configFileNode.finishIfStarted();
    }
    tree = null;
  };
}

function normalizeAssertionError(stack, assertionError) {
  if (!assertionError) {
    return normalizeErrorStack(stack);
  }
  stack = stack || '';
  var assertionMessage = assertionError.message;
  var assertionName = assertionError.name;
  var stackLeftTrimmed = stack.trimStart();
  if (util.isString(assertionMessage) && stackLeftTrimmed.indexOf(assertionMessage) === 0) {
    stack = stackLeftTrimmed.substring(assertionMessage.length);
  }
  if (util.isString(assertionName) && util.isString(assertionMessage)) {
    var compoundMessage = assertionName + ': ' + assertionMessage;
    if (stackLeftTrimmed.indexOf(compoundMessage) === 0) {
      assertionMessage = compoundMessage;
      stack = stackLeftTrimmed.substring(compoundMessage.length);
    }
  }
  if (stack.length > 0 && stack.charAt(0) === '\n') {
    stack = stack.substring(1);
  }
  return {
    message: assertionMessage,
    stack: stack,
    expected: assertionError.expected,
    actual: assertionError.actual
  };
}

function normalizeErrorStack(stack) {
  const stackLines = splitByLines(stack.trimStart());
  if (stackLines.length > 0) {
    const firstLine = stackLines[0];
    if (!firstLine.startsWith("at ")) {
      return {
        message: firstLine,
        stack: stackLines.slice(1).join('\n')
      }
    }
  }
  return {
    stack: stack
  }
}

function createStack(logs, formatError) {
  const lines = logs.map(log => formatError(log, util.isString(log) ? '' : '\t'));
  return lines.join('');
}

function splitByLines(text) {
  return text.split(/\n|\r\n/);
}

IntellijReporter.$inject = ['config', 'fileList', 'formatError', 'emitter', 'injector'];

IntellijReporter.reporterName = 'intellij_c831a91b03572bad3b3db88354641e3b';

module.exports = IntellijReporter;
