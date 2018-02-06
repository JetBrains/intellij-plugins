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
var currentQuestion;

var port = parseInt(process.argv[process.argv.length - 1], 10);
var rpcServer = rpcNode.connect(port, {
    "vue-create-project": {
        "answer": function(answer) {
            var validator = currentQuestion["validate"];
            var validation = validator == null || validator(answer);
            if (validation !== true) {
                sendQuestion(currentQuestion, validation);
            } else {
                if (currentQuestion["type"] === "confirm" && answer === "Yes") {
                    var resConfirm = {};
                    resConfirm[currentQuestion["name"]] = true;
                    currentResolve(resConfirm);
                } else {
                    var res = {};
                    res[currentQuestion["name"]] = answer;
                    currentResolve(res);
                }
                currentReject = null;
                currentResolve = null;
            }
        },
        "start": function(pathToVueCli, projectTemplate, projectName) {
            startCreation(pathToVueCli, projectTemplate, projectName);
        },
        "cancel": function() {
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
    if (rpcServer != null) rpcServer.send(DOMAIN, "question", JSON.stringify(obj), error);
}

function calcBasicPath(path) {
    if (path.charAt(path.length - 1) !== '/' &&
        path.charAt(path.length - 1) !== '\\') {
        path = path + '/';
    }
    path = path.split("\\").join("/");
    return path
}

function startCreation(pathToVueCli, projectTemplate, projectName) {
    process.argv[1] = "init";
    process.argv[2] = projectTemplate; //type
    process.argv[3] = projectName; // default name

    pathToVueCli = calcBasicPath(pathToVueCli);

    var fs = require("fs");
    var inquirer;
    if (fs.existsSync(pathToVueCli + "inquirer")) {
        inquirer = require(pathToVueCli + "inquirer");
    } else if (fs.existsSync(pathToVueCli + "vue-cli/node_modules/inquirer")) {
        inquirer = require(pathToVueCli + "vue-cli/node_modules/inquirer");
    } else {
        rpcServer.send(DOMAIN, "error", "Can not find inquirer!");
        return;
    }

    inquirer.prompt = function () {
        var obj = arguments[0][0];
        if (obj == null) {
            rpcServer.send(DOMAIN, "error", "Can not parse question: " + JSON.stringify(arguments[0]));
            reject("parsing error");
            return;
        }
        currentQuestion = obj;

        return new Promise(function(resolve, reject) {
            currentResolve = resolve;
            currentReject = reject;
            sendQuestion(obj, null);
        });
    };
    var logger = require(pathToVueCli + "vue-cli/lib/logger");
    var oldSuccess = logger.success;
    logger.success = function() {
        if (arguments.length > 0 && arguments[0].startsWith("Generated")) {
          rpcServer.send(DOMAIN, "questionsFinished");
          logger.success = oldSuccess;
        }
        Object.apply(oldSuccess, arguments);
    };
    var vue_init = require(pathToVueCli + "vue-cli/bin/vue-init");
}

rpcServer.send(DOMAIN, "notifyStarted");
