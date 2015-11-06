var doEscapeCharCode = (function () {
  var obj = {};

  function addMapping(fromChar, toChar) {
    if (fromChar.length !== 1 || toChar.length !== 1) {
      throw Error('String length should be 1');
    }
    var fromCharCode = fromChar.charCodeAt(0);
    if (typeof obj[fromCharCode] === 'undefined') {
      obj[fromCharCode] = toChar;
    }
    else {
      throw Error('Bad mapping');
    }
  }

  addMapping('\n', 'n');
  addMapping('\r', 'r');
  addMapping('\u0085', 'x');
  addMapping('\u2028', 'l');
  addMapping('\u2029', 'p');
  addMapping('|', '|');
  addMapping('\'', '\'');
  addMapping('[', '[');
  addMapping(']', ']');

  return function (charCode) {
    return obj[charCode];
  };
}());

function isAttributeValueEscapingNeeded(str) {
  var len = str.length;
  for (var i = 0; i < len; i++) {
    if (doEscapeCharCode(str.charCodeAt(i))) {
      return true;
    }
  }
  return false;
}

function attributeValueEscape(str) {
  if (!isAttributeValueEscapingNeeded(str)) {
    return str;
  }
  var res = ''
    , len = str.length;
  for (var i = 0; i < len; i++) {
    var escaped = doEscapeCharCode(str.charCodeAt(i));
    if (escaped) {
      res += '|';
      res += escaped;
    }
    else {
      res += str.charAt(i);
    }
  }
  return res;
}

/**
 * @param {Array} list
 * @param {Number} fromInclusive
 * @param {Number} toExclusive
 * @param {String} delimiterChar one character string
 * @returns {String}
 */
function joinList(list, fromInclusive, toExclusive, delimiterChar) {
  if (list.length === 0) {
    return '';
  }
  if (delimiterChar.length !== 1) {
    throw Error('Delimiter is expected to be a character, but "' + delimiterChar + '" received');
  }
  var addDelimiter = false
    , escapeChar = '\\'
    , escapeCharCode = escapeChar.charCodeAt(0)
    , delimiterCharCode = delimiterChar.charCodeAt(0)
    , result = ''
    , item
    , itemLength
    , ch
    , chCode;
  for (var itemId = fromInclusive; itemId < toExclusive; itemId++) {
    if (addDelimiter) {
      result += delimiterChar;
    }
    addDelimiter = true;
    item = list[itemId];
    itemLength = item.length;
    for (var i = 0; i < itemLength; i++) {
      ch = item.charAt(i);
      chCode = item.charCodeAt(i);
      if (chCode === delimiterCharCode || chCode === escapeCharCode) {
        result += escapeChar;
      }
      result += ch;
    }
  }
  return result;
}

/**
 * Sends event to the IDE through standard output of karma server process.
 * @param {String} eventType
 * @param {Object} eventBody
 */
function sendIntellijEvent(eventType, eventBody) {
  process.stdout.write('##intellij-event[' + eventType + ':' + JSON.stringify(eventBody) + ']\n');
}

var toString = {}.toString;

function isString(value) {
  return typeof value === 'string' || toString.call(value) === '[object String]';
}

/**
 *
 * @param processor this function gets called with line as a single parameter.
 *                  If the function returns false, standard input processing is stopped.
 */
function processStdInput(processor) {
  process.stdin.resume();
  process.stdin.setEncoding('utf8');
  var text = '';
  var listener = function (data) {
    text += data;
    var startInd = 0;
    var proceed = true;
    while (true) {
      var nlInd = text.indexOf('\n', startInd);
      if (nlInd < 0) {
        break;
      }
      var line = text.substring(startInd, nlInd);
      proceed = processor(line);
      if (!proceed) {
        break;
      }
      startInd = nlInd + 1;
    }
    text = text.substring(startInd);
    if (!proceed) {
      process.stdin.removeListener('data', listener);
      process.stdin.pause();
      process.stdin.destroy();
    }
  };
  process.stdin.on('data', listener);
}

/**
 * Removes from the passed 'elements' array all elements from 'elementsToRemove' and returns it.
 * Doesn't modify 'elements' array.
 *
 * @param elements
 * @param {Array|string} elementsToRemove
 * @returns {Array}
 */
function removeAll(elements, elementsToRemove) {
  if (Array.isArray(elementsToRemove)) {
    return elements.filter(function (element) {
      return elementsToRemove.indexOf(element) < 0;
    });
  }
  else {
    return elements.filter(function (element) {
      return elementsToRemove !== element;
    });
  }
}

/**
 * Checks whether the given preprocessor specified at least once in 'config.preprocessors'.
 *
 * @param {Object} preprocessors
 * @param {String} preprocessorName
 * @return {boolean} true, if the given preprocessor specified at least once in 'preprocessors'
 */
function isPreprocessorSpecified(preprocessors, preprocessorName) {
    if (preprocessors != null) {
        for (var key in preprocessors) {
            if (Object.prototype.hasOwnProperty.call(preprocessors, key)) {
                var value = preprocessors[key];
                if (value === preprocessorName) {
                    return true;
                }
                if (Array.isArray(value) && value.indexOf(preprocessorName) >= 0) {
                    return true;
                }
            }
        }
    }
    return false;
}

exports.attributeValueEscape = attributeValueEscape;
exports.joinList = joinList;
exports.sendIntellijEvent = sendIntellijEvent;
exports.isString = isString;
exports.processStdInput = processStdInput;
exports.removeAll = removeAll;
exports.isPreprocessorSpecified = isPreprocessorSpecified;
