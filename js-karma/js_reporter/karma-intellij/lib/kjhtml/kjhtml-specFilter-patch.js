/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

// Workaround for https://github.com/taras42/karma-jasmine-html-reporter/issues/25

var KJHTML_PATTERN = /([\\/]karma-jasmine-html-reporter[\\/])/i;
var JASMINE_ADAPTER = /([\\/]karma-jasmine[\\/]lib[\\/]adapter\.js$)/i;
var MOCHA_ADAPTER = /([\\/]karma-mocha[\\/]lib[\\/]adapter\.js$)/i;

var createPattern = function(path) {
  return {pattern: path, included: true, served: true, watched: false};
};

exports.apply = function (configFiles) {
  var startInd = -1;
  var endInd = -1;
  configFiles.forEach(function(file, index) {
    if (KJHTML_PATTERN.test(file.pattern)) {
      if (startInd === -1) {
        startInd = index;
      }
      endInd = index;
    }
  });
  if (startInd !== -1) {
    configFiles.splice(startInd, 0, createPattern(__dirname + '/intellij-save-specFilter-before-kjhtml.js'));
    endInd++;
    configFiles.splice(endInd + 1, 0, createPattern(__dirname + '/intellij-restore-specFilter-after-kjhtml.js'))
  }
  var jasmineAdapterInd = findPatternInd(configFiles, JASMINE_ADAPTER);
  var mochaAdapterInd = findPatternInd(configFiles, MOCHA_ADAPTER);
  // Insert karma-intellij-adapter.js right after jasmine/mocha adapter to override specified specFilter/grep.
  // If no jasmine/mocha adapter found, insert karma-intellij-adapter.js at the beginning of the list:
  //   jasmine/mocha adapter will be inserted before karma-intellij-adapter.js later.
  configFiles.splice(Math.max(jasmineAdapterInd, mochaAdapterInd) + 1, 0, createPattern(__dirname + '/karma-intellij-adapter.js'));
};

function findPatternInd(configFiles, filePattern) {
  return configFiles.findIndex((file) => filePattern.test(file.pattern));
}
