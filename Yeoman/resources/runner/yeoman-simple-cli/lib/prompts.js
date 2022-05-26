var cli = require('./cli');
var debug = cli.debugFunction;

function confirmHandler(questionWithAnswers, userReplayString) {
  function convertStringReplayToBoolean() {
    if (userReplayString === "1") return true;
    if (userReplayString === "0") return false;
    var s = userReplayString.trim().toLocaleLowerCase();
    return (s === "y" || s === "yes" || s === "true");
  }

  if (isUseDefault(userReplayString)) {
    debug('set default answer');
    return questionWithAnswers.default || false;
  }

  return convertStringReplayToBoolean();
}


function inputHandler(questionWithAnswers, userReplayString) {
  if (isUseDefault(userReplayString)) {
    debug('set default answer');
    return questionWithAnswers.default || "";
  }

  return userReplayString.trim();
}

function checkboxHandler(questionWithAnswers, userReplayString) {
  console.log('input ' + userReplayString + ' handle')
  var choices = questionWithAnswers.choices;

  var arrResult = [];
  if (isUseDefault(userReplayString)) {
    choices.forEach(function (v) {
      if (v.checked) {
        arrResult.push(choiceToResult(v));
      }
    });
  }
  else {
    userReplayString.trim().split(",").forEach(function (v) {
      if (v !== null) {
        var choice = choices[getNumber(v)];
        arrResult.push(choiceToResult(choice));
      }
    })
  }

  return arrResult;
}

function listHandler(questionWithAnswers, userReplayString) {
  var choices = questionWithAnswers.choices;

  debug("User text " + userReplayString)

  var result;
  if (isUseDefault(userReplayString)) {
    var defaultValue = questionWithAnswers.default;

    var index = 0;
    if (typeof defaultValue === 'number') {
      index = defaultValue;
    }
    else if (typeof defaultValue === 'string') {
      var i = 0;
      choices.forEach(function (v) {
        if (v.value === defaultValue) {
          index = i;
        }
        i++;
      })
    }
    result = choices[index];
  }
  else {
    result = choices[getNumber(userReplayString) || 0];
  }


  var valueToReturn = (typeof result === 'string' ? result : choiceToResult(result));
  debug("Converted value " + valueToReturn);
  return postProcess(questionWithAnswers, valueToReturn);
}

function postProcess(questionWithAnswers, result) {
  if (questionWithAnswers.filter) {
    return questionWithAnswers.filter(result);
  }

  return result;
}

function expandHandler(questionWithAnswers, userReplayString) {
  return listHandler(questionWithAnswers, userReplayString);
}

function isUseDefault(userReplayString) {
  return userReplayString === '\n' || userReplayString === '';
}

function choiceToResult(choice) {
  return choice.value || choice.name;
}

function getNumber(rawNumber) {
  if (typeof rawNumber === 'number') {
    return rawNumber;
  }

  try {
    return parseInt(rawNumber.trim());
  }
  catch (e) {
    debug(e);
  }
}


module.exports = {
  defaultHandler: confirmHandler,
  handlers: {
    confirm: confirmHandler,
    input: inputHandler,
    checkbox: checkboxHandler,
    list: listHandler,
    rawlist: listHandler,
    expand: listHandler
  }
}
