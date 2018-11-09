// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var DOMAIN = "vue-create-project";
var rpcNode = require("ij-rpc-client");
var currentResolve;
var currentReject;
var questions;
var idx = 0;
var answers = {};
var isOldCli = false;

var port = parseInt(process.argv[process.argv.length - 1], 10);
var rpcServer = rpcNode.connect(port, {
  "vue-create-project": {
    answer: function (answer) {
      var current = questions[idx];
      var validator = current["validate"];
      var validation = validator == null || validator(answer);
      if (validation !== true) {
        sendQuestion(current, validation);
      }
      else {
        if (current["type"] === "confirm") {
          answers[current["name"]] = answer === "Yes";
        }
        else {
          answers[current["name"]] = answer;
        }
        ++idx;
        while (questions[idx] != null && idx < questions.length &&
               questions[idx].when != null && !questions[idx].when(answers)) {
          ++idx;
        }
        if (idx === questions.length) {
          currentResolve(answers);
          currentReject = null;
          currentResolve = null;
          if (!isOldCli) {
            rpcServer.send(DOMAIN, "questionsFinished");
          }
        }
        else {
          sendQuestion(questions[idx], null);
        }
      }
    },
    answerCheckbox: function (answerArray) {
      this.answer(answerArray);
    },
    "start": function (pathToVueCli, packageName, projectTemplate, projectName) {
      isOldCli = packageName === "vue-cli";
      startCreation(pathToVueCli, packageName, projectTemplate, projectName);
    },
    "cancel": function () {
      if (currentReject != null) {
        currentReject("cancelled");
      }
      if (rpcServer != null) {
        rpcNode.close(rpcServer);
        rpcServer = null;
      }
    }
  }
});

function sendQuestion(obj, error) {
  var copy = obj;
  if (obj.choices instanceof Function) {
    copy = JSON.parse(JSON.stringify(obj));
    copy.choices = obj.choices(answers);
  }
  if (rpcServer != null) rpcServer.send(DOMAIN, "question", JSON.stringify(copy), error);
}

function calcBasicPath(path) {
  if (path.charAt(path.length - 1) !== '/' &&
      path.charAt(path.length - 1) !== '\\') {
    path = path + '/';
  }
  path = path.split("\\").join("/");
  return path
}

function startCreation(pathToVueCli, packageName, projectTemplate, projectName) {
  if (isOldCli) {
    process.argv[1] = "init";
    process.argv[2] = projectTemplate; //type
    process.argv[3] = projectName; // default name
  }

  pathToVueCli = calcBasicPath(pathToVueCli);

  var fs = require("fs");
  var inquirer;
  if (fs.existsSync(pathToVueCli + packageName + "/node_modules/inquirer")) {
    inquirer = require(pathToVueCli + packageName + "/node_modules/inquirer");
  }
  else if (fs.existsSync(pathToVueCli + "inquirer")) {
    inquirer = require(pathToVueCli + "inquirer");
  }
  else {
    rpcServer.send(DOMAIN, "error", "Can not find inquirer!");
    return;
  }

  inquirer.prompt = function () {
    if (arguments[0] == null) {
      rpcServer.send(DOMAIN, "error", "Can not parse question: " + JSON.stringify(arguments[0]));
      reject("parsing error");
      return;
    }
    questions = arguments[0];
    idx = 0;
    answers = {};

    return new Promise(function (resolve, reject) {
      currentResolve = resolve;
      currentReject = reject;
      sendQuestion(questions[idx], null);
    });
  };
  if (isOldCli) {
    var logger = require(pathToVueCli + "vue-cli/lib/logger");
    var oldSuccess = logger.success;
    logger.success = function () {
      if (arguments.length > 0 && arguments[0].startsWith("Generated")) {
        rpcServer.send(DOMAIN, "questionsFinished");
        logger.success = oldSuccess;
      }
      Object.apply(oldSuccess, arguments);
    };
    var vue_init = require(pathToVueCli + "vue-cli/bin/vue-init");
  }
  else {
    var vue_create = require(pathToVueCli + packageName + "/lib/create")(projectName, {});
  }
}

rpcServer.send(DOMAIN, "notifyStarted");
