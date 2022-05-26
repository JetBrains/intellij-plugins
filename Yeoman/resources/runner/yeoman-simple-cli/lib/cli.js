var path = require('path');


module.exports = {debugFunction: debug}
var prompts = require('./prompts');
process.stdin.setEncoding('utf8');

function parseArgs(args) {
  if (!args || args.length === 0) {
    console.log('Possible options:');
    console.log('--generatorName NAME - name with namespace of the generator (Required)');
    console.log('--generatorPath PATH - path to generator folder');
    console.log('--yeoman PATH - path to yeoman nodejs package');
    console.log('--plain - by default output uses json format. If you want to use plain string specify the parameter');
    console.log('--arguments - generator arguments');
    console.log('--noskipinstall - do not pass skip install argument');
    console.log('--debug - debug output');
    console.log('--newYoVersion - use new (Promise) Yeoman API');
    console.log(
        'example :node lib/cli.js --generatorName angular-fullstack --yeoman /usr/local/lib/node_modules/yo --plain --debug --newYoVersion\n')
    process.exit(0);
  }


  var options = {}

  for (var i = 0; i < args.length; i++) {
    switch (args[i]) {
      case "--generatorPath": {
        options.generatorPath = (++i < args.length) ? args[i] : null;
        break;
      }
      case "--generatorName": {
        options.generatorName = (++i < args.length) ? args[i] : null;
        break;
      }
      case "--yeoman": {
        options.yeoman = (++i < args.length) ? args[i] : null;
        break;
      }
      case "--arguments": {
        options.arguments = (++i < args.length) ? args[i] : null;
        break;
      }
      case "--plain": {
        options.plain = true;
        break;
      }
      case "--noskipinstall": {
        options.noskipinstall = true;
        break;
      }
      case "--debug": {
        options.debug = true;
        break;
      }
      case "--newYoVersion": {
        options.newYoVersion = true;
        break;
      }
      default: //do nothing
    }
  }

  return options;
}

var options = parseArgs(process.argv.slice(2));

var debugSupport = options.debug ? true : false;

if (options.arguments && !options.noskipinstall) {
  options.arguments = options.arguments.trim() + " --skip-install";
}


var optionsForRun = {};
if (options.arguments) {
  var argsForGenerator = options.arguments.split(' ');
  for (var i = 0; i < argsForGenerator.length; i++) {
    var value = argsForGenerator[i];
    if (value.indexOf('--') === 0) {
      var nextIndex = i + 1;
      var keyDefault = value.substring(2);
      var key = camelCase(keyDefault);
      if (nextIndex < argsForGenerator.length && argsForGenerator[nextIndex].indexOf('--') === -1) {
        optionsForRun[key] = argsForGenerator[++i];
      }
      else {
        optionsForRun[key] = true;
      }
      optionsForRun[keyDefault] = optionsForRun[key];
    }
  }
}
else if (!options.noskipinstall) {
  optionsForRun['skipInstall'] = true;
  optionsForRun['skip-install'] = true;
}

debug('options for generator ' + JSON.stringify(optionsForRun));

var prefix = (options.yeoman ? options.yeoman + path.sep + 'node_modules' + path.sep : '');
var inquirer = require(prefix + 'inquirer');
var yeomanEnv = require(prefix + 'yeoman-environment');


var env = yeomanEnv.createEnv(null, optionsForRun, new Adapter);

var wasDone = false;


var callbackDone = function () {
  debug("DONE<> as generator")
  wasDone = true;
  setWaitingNextInput();
}


var cwd = process.cwd();

var args = options.generatorName;
if (options.generatorPath) {
  process.chdir(options.generatorPath);
  env.lookup(function () {
    process.chdir(cwd);
    runGenerator();
  });
}
else if (options.generatorName) {
  env.lookup(function () {
    runGenerator();
  });
}
else {
  throw new Error("You should specify generatorName");
}

function runGenerator() {
  debug('invoke run');
  env.run(args, optionsForRun, callbackDone);
}

/**
 * @constructor
 */
function Adapter() {
  debug('adapter create');


  this.promptModule = inquirer.createPromptModule();

  Object.keys(this.promptModule.prompts).forEach(function (name) {
    this.promptModule.registerPrompt(name, Prompt.bind(null, prompts.handlers[name] || prompts.defaultHandler));
  }, this);
  ;


  if (options.newYoVersion) {
    this.prompt = (function (questions, cb) {
      const promise = this.promptModule(questions);
      promise.then(function () {

        if (cb) {
          debug("Run original callback for " + (arguments.length > 0 ? arguments[0] : ""));
          cb.apply(null, arguments);
        }
      });
      return promise;
    }).bind(this);
  }
  else {
    this.prompt = this.promptModule;
  }

  this.log = yeomanEnv.util.log();

}


/**
 * @constructor
 */
function Prompt(getResultFunction, question, rl, answers) {
  this.question = question;
  this.rl = rl;
  this.answers = answers;

  this._run = function (callback) {
    printMessage(question);

    waitingInput = true;

    var listener = function (line) {
      var resultFunction = getResultFunction(question, line);
      callback(resultFunction);
      rl.removeListener('line', listener);

      if (wasDone) {
        setWaitingNextInput();
      }
      waitingInput = false;
    };

    rl.on('line', listener);

    return this;
  }

  if (options.newYoVersion) {
    this.run = function () {
      return new Promise(function (resolve) {
        this._run(function (callback) {
          debug("resolve called");
          return resolve(callback)
        })
      }.bind(this));
    }
  }
  else {
    this.run = this._run;
  }


  return this;
}


function printMessage(questionWithAnswers) {
  if (options.plain) {
    //print plain text
    process.stdout.write(questionWithAnswers.message + '\n');
    if (questionWithAnswers.choices) {
      var index = 0;
      questionWithAnswers.choices.forEach(function (v) {
        if (typeof v === 'string') {
          process.stdout.write('' + (index++) + ' ' + v + '\n')
        }
        else {
          process.stdout.write('' + (index++) + ' ' + (v.name || v.value || v.message) + (v.checked ? ' x' : '') + '\n');
        }
      });
    }
  }
  else {
    var stringAnswers = JSON.stringify(questionWithAnswers);
    process.stdout.write(stringAnswers + '\n');
  }
}


function debug(v) {
  if (debugSupport) console.log('--debug ' + v);
}

function camelCase(str) {
  str = str.trim();

  if (str.length === 1 || !(/[_.\- ]+/).test(str)) {
    if (str[0] === str[0].toLowerCase() && str.slice(1) !== str.slice(1).toLowerCase()) {
      return str;
    }

    return str.toLowerCase();
  }

  return str
      .replace(/^[_.\- ]+/, '')
      .toLowerCase()
      .replace(/[_.\- ]+(\w|$)/g, function (m, p1) {
        return p1.toUpperCase();
      });
}


//magic hack for shutdown the process
var waitingInput = false;
var timeout = null;

function setWaitingNextInput() {
  if (timeout !== null) {
    clearTimeout(timeout);
  }
  timeout = setTimeout(function () {
    timeout = null;
    waiting = false;
    if (!waitingInput) {
      process.stdin.destroy();
    }
  }, 500);
}


