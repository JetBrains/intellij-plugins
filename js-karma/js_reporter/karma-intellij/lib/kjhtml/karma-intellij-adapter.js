/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

(function (window) {

  'use strict';

  /**
   * Extract test name pattern from karma config.
   * @param {object} karmaConfig The karma config object
   * @return {string} test name pattern string, or undefined if unspecified
   */
  var getTestNamePatternString = function (karmaConfig) {
    var clientArguments = (karmaConfig || {}).args;
    if (Object.prototype.toString.call(clientArguments) === '[object Array]') {
      var grepRegex = /^--testNamePattern_intellij=(.*)$/;
      return map(filter(clientArguments, function (arg) {
        return grepRegex.test(arg);
      }), function (arg) {
        return arg.replace(grepRegex, '$1');
      })[0];
    }
  };

  /**
   * @param {Object} config The karma config
   * @param {Object} jasmineEnv jasmine environment object
   */
  var setJasmineSpecFilter = function (config, jasmineEnv) {
    var testNamePatternString = getTestNamePatternString(config);
    if (testNamePatternString) {
      // Configure --grep for karma-jasmine, otherwise it will reset specFilter before executing tests.
      config.args.push("--grep=/" + testNamePatternString + "/");
    }
    var testNamePattern = testNamePatternString ? createRegExp(testNamePatternString) : null;
    var specFilter = function (spec) {
      return testNamePattern == null || testNamePattern.test(spec.getFullName());
    };
    if (typeof jasmineEnv.configuration === 'function') {
      var configuration = jasmineEnv.configuration() || {};
      configuration.specFilter = specFilter;
      jasmineEnv.configure(configuration);
    }
    else {
      jasmineEnv.specFilter = specFilter;
    }
  };

  /**
   * @param {Object} config The karma config
   * @param {Object} mocha mocha global object
   */
  function setMochaSpecFilter(config, mocha) {
    var grepOption = getTestNamePatternString(config);
    if (grepOption) {
      mocha.grep(createRegExp(grepOption));
    }
  }

  function createRegExp(testNamePatternString) {
    testNamePatternString = testNamePatternString || ''
    if (testNamePatternString === '') {
      return new RegExp(".*") // to match all
    }
    return new RegExp(testNamePatternString)
  }

  function filter(collection, filter, that /* opt*/) {
    if (collection.filter) {
      return collection.filter(filter, that)
    }

    var other = []
    var v
    for (var i = 0, n = collection.length; i < n; i++) {
      if (i in collection && filter.call(that, v = collection[i], i, collection)) {
        other.push(v)
      }
    }
    return other
  }

  function map(collection, mapper, that /* opt*/) {
    if (collection.map) {
      return collection.map(mapper, that)
    }

    var other = new Array(collection.length)
    for (var i = 0, n = collection.length; i < n; i++) {
      if (i in collection) {
        other[i] = mapper.call(that, collection[i], i, collection)
      }
    }
    return other
  }

  if (window.jasmine) {
    setJasmineSpecFilter(window.__karma__.config, window.jasmine.getEnv())
  }

  if (window.mocha) {
    setMochaSpecFilter(window.__karma__.config, window.mocha);
  }

})(window);
