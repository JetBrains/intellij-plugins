/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

(function (window) {

  'use strict';

  /**
   * Extract grep option from karma config
   * @param {[Array|string]} clientArguments The karma client arguments
   * @return {string} The value of grep option by default empty string
   */
  var getGrepOption = function (clientArguments) {
    var grepRegex = /^--grep=(.*)$/;

    if (Object.prototype.toString.call(clientArguments) === '[object Array]') {
      var indexOfGrep = indexOf(clientArguments, '--grep');

      if (indexOfGrep !== -1) {
        return clientArguments[indexOfGrep + 1]
      }

      return map(filter(clientArguments, function (arg) {
        return grepRegex.test(arg)
      }), function (arg) {
        return arg.replace(grepRegex, '$1')
      })[0] || ''
    }
    else if (typeof clientArguments === 'string') {
      var match = /--grep=([^=]+)/.exec(clientArguments);

      return match ? match[1] : ''
    }
  };

  /**
   * @param {Object} config The karma config
   * @param {Object} jasmineEnv jasmine environment object
   */
  var setJasmineSpecFilter = function (config, jasmineEnv) {
    var grepOption = getGrepOption((config || {}).args);
    var filterPattern = grepOption ? createRegExp(grepOption) : null;
    var specFilter = function (spec) {
      return filterPattern == null || filterPattern.test(spec.getFullName());
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
    var grepOption = getGrepOption((config || {}).args);
    if (grepOption) {
      mocha.grep(createRegExp(grepOption));
    }
  }

  function createRegExp(filter) {
    filter = filter || ''
    if (filter === '') {
      return new RegExp() // to match all
    }
    var regExp = /^[/](.*)[/]$/ // pattern to check whether the string is RegExp pattern
    var parts = regExp.exec(filter)
    if (parts === null) {
      return new RegExp(filter)
    }
    return new RegExp(parts[1])
  }

  function indexOf(collection, find, i /* opt*/) {
    if (collection.indexOf) {
      return collection.indexOf(find, i)
    }

    if (i === undefined) {
      i = 0
    }
    if (i < 0) {
      i += collection.length
    }
    if (i < 0) {
      i = 0
    }
    for (var n = collection.length; i < n; i++) {
      if (i in collection && collection[i] === find) {
        return i
      }
    }
    return -1
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
