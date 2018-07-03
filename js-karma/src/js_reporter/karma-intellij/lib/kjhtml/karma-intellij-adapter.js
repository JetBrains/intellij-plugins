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

  var createRegExp = function (testNamePattern) {
    testNamePattern = testNamePattern || '';
    if (testNamePattern === '') {
      return null;
    }
    return new RegExp(testNamePattern);
  };

  /**
   * @param {Object} config The karma config
   * @param {Object} jasmineEnv jasmine environment object
   */
  var createSpecFilter = function (config, jasmineEnv) {
    var grepOption = getGrepOption(config.args);
    var filterPattern = createRegExp(grepOption);

    jasmineEnv.specFilter = function (spec) {
      return filterPattern == null || filterPattern.test(spec.getFullName());
    }
  };

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
    createSpecFilter(window.__karma__.config, window.jasmine.getEnv())
  }

})(window);
