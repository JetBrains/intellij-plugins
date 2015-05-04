var cli = require("./intellijCli.js")
  , intellijUtil = require('./intellijUtil.js')
  , util = require('util')
  , Tree = require('./tree.js')
  , FileListUpdater = require('./fileListUpdater').FileListUpdater;

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
  browserErrorNode.setStatus(3, null, error);
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

function sendBrowserEvents(eventType, connectionId2BrowserObjA, connectionId2BrowserObjB, addAutoCapturingInfo) {
  for (var connectionId in connectionId2BrowserObjA) {
    if (connectionId2BrowserObjA.hasOwnProperty(connectionId)) {
      if (!connectionId2BrowserObjB.hasOwnProperty(connectionId)) {
        var browser = connectionId2BrowserObjA[connectionId];
        var event = {id: connectionId, name: browser.name};
        if (addAutoCapturingInfo) {
          event.isAutoCaptured = isAutoCapturedBrowser(browser);
        }
        intellijUtil.sendIntellijEvent(eventType, event);
      }
    }
  }
}

function isAutoCapturedBrowser(browser) {
  if (browser.launchId != null) {
    return true;
  }
  var idStr = browser.id;
  if (intellijUtil.isString(idStr)) {
    return /^\d+$/.test(idStr);
  }
  return false;
}

function startBrowsersTracking(globalEmitter) {
  var oldConnectionId2BrowserObj = {};
  globalEmitter.on('browsers_change', function(capturedBrowsers) {
    if (!capturedBrowsers.forEach) {
      // filter out events from Browser object
      return;
    }
    var newConnectionId2BrowserObj = {};
    var proceed = true;
    capturedBrowsers.forEach(function(newBrowser) {
      if (!newBrowser.id || !newBrowser.name || newBrowser.id === newBrowser.name) {
        proceed = false;
      }
      newConnectionId2BrowserObj[newBrowser.id] = newBrowser;
    });
    if (proceed) {
      sendBrowserEvents('browserConnected', newConnectionId2BrowserObj, oldConnectionId2BrowserObj, true);
      sendBrowserEvents('browserDisconnected', oldConnectionId2BrowserObj, newConnectionId2BrowserObj, false);
      oldConnectionId2BrowserObj = newConnectionId2BrowserObj;
    }
  });
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
  var logManager = new LogManager();
  new FileListUpdater(config, fileList);
  startBrowsersTracking(globalEmitter);
  this.adapters = [];
  var totalTestCount, uncheckedBrowserCount;

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

    totalTestCount = 0;
    uncheckedBrowserCount = browsers.length;
    beforeRunStart = false;
    tree = new Tree(cli.getConfigFile(), write);
    process.nextTick(function() {
      tree.write('##teamcity[enteredTheMatrix]\n');
    });
  };

  this.onBrowserError = function (browser, error) {
    if (tree == null) {
      // https://youtrack.jetbrains.com/issue/WEB-16291
      var message = beforeRunStart ? '"onBrowserError" unexpectedly called before "onRunStart"'
                                   : '"onBrowserError" unexpectedly called after "onRunComplete"';
      console.error('[karma bug found] ' + message + ", logged by karma-intellij plugin");
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
      return;
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
    if (typeof browserNode.checkedForTotalTestCount === 'undefined') {
      browserNode.checkedForTotalTestCount = true;
      totalTestCount += browser.lastResult.total;
      uncheckedBrowserCount--;
      if (uncheckedBrowserCount === 0) {
        tree.write('##teamcity[testCount count=\'' + totalTestCount + '\']\n');
      }
    }
    var suiteNode = getOrCreateLowerSuiteNode(browserNode, suiteNames, write);
    var specNode = createSpecNode(suiteNode, suiteNames, specName);
    var status = result.success ? 0 : 2;
    var failureMsg = '';
    result.log.forEach(function (log) {
      failureMsg += formatError(log, '\t');
    });
    specNode.setStatus(status, result.time, failureMsg);
    logManager.attachTo(specNode);
    specNode.writeFinishMessage();
  };

  this.onRunComplete = function (browsers, results) {
    logManager.attachToAnything(tree);
    tree.configFileNode.finishIfStarted();
    tree = null;
  };
}

IntellijReporter.$inject = ['config', 'fileList', 'formatError', 'emitter', 'injector'];

IntellijReporter.reporterName = 'intellij_c831a91b03572bad3b3db88354641e3b';

module.exports = IntellijReporter;
