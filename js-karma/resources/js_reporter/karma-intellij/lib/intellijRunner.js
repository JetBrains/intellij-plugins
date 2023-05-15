/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var cli = require('./intellijCli')
  , intellijUtil = require('./intellijUtil')
  , RESUME_TEST_RUNNING_MESSAGE = 'resume-test-running'
  , EXIT_CODE_BUF = Buffer.from('\x1FEXIT');

/**
 * @param {Buffer} buffer
 * @returns {Buffer}
 */
function stripExitCodeInfo(buffer) {
  if (!Buffer.isBuffer(buffer)) {
    return buffer;
  }
  var tailPos = buffer.length - EXIT_CODE_BUF.length - 2;
  if (tailPos < 0) {
    return buffer;
  }
  for (var i = 0; i < EXIT_CODE_BUF.length; i++) {
    if (buffer[tailPos + i] !== EXIT_CODE_BUF[i]) {
      return buffer;
    }
  }
  if (tailPos === 0) {
    return null;
  }
  return buffer.slice(0, tailPos);
}

function runWithConfig(config) {
  var options = {
    hostname: config.host,
    path: config.urlRoot + 'run',
    port: config.port,
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    rejectUnauthorized: false
  };

  var http = cli.getProtocol() === 'https:' ? require('https') : require('http');

  var request = http.request(options, function(response) {
    response.on('data', function(buffer) {
      var out = stripExitCodeInfo(buffer);
      if (out != null) {
        process.stdout.write(out);
      }
    });
  });

  request.on('error', function(e) {
    if (e.code === 'ECONNREFUSED') {
      console.error('There is no server listening on port %d', options.port);
    } else {
      throw e;
    }
  });

  request.end(JSON.stringify({
    args: config.clientArgs,
    removedFiles: config.removedFiles,
    changedFiles: config.changedFiles,
    addedFiles: config.addedFiles,
    refresh: config.refresh
  }));
}

function runTests() {
  var urlRoot = cli.getUrlRoot() || '/';
  if (urlRoot.charAt(urlRoot.length - 1) !== '/') {
    urlRoot = urlRoot + '/';
  }
  var config = {
    host: cli.getServerHost(),
    port: cli.getServerPort(),
    refresh: true,
    urlRoot: urlRoot
  };
  var testNamePattern = cli.getTestNamePattern();
  // if testName is undefined, reset jasmine.getEnv().specFilter function
  // otherwise, last specified specFilter will be used
  config.clientArgs = ['--testNamePattern_intellij=' + (testNamePattern || '')];
  runWithConfig(config);
}

if (cli.isDebug()) {
  intellijUtil.processStdInput(function(line) {
    var resume = RESUME_TEST_RUNNING_MESSAGE === line;
    if (resume) {
      runTests();
    }
    return !resume;
  });
}
else {
  runTests();
}
