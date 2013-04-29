var path = require('path');

function inherit(child, parent) {
  function F() {
    this.constructor = child;
  }

  F.prototype = parent.prototype;
  child.prototype = new F();
  return child;
}


function Tree(configFilePath, write) {
  this.configFileNode = new TestSuiteNode(this, 1, null, path.basename(configFilePath), 'config');
  this.write = write;
  this.nextId = 2;
}

function Node(tree, id, parentNode, name, type) {
  this.tree = tree;
  this.id = id;
  this.parentNode = parentNode;
  this.name = name;
  this.type = type;
  this.isFinished = false;
}

Node.prototype.getExtraFinishMessageParameters = function () {
  return null;
};

Node.prototype.finishIfStarted = function () {
  if (!this.isFinished) {
    for (var i = 0; i < this.children.length; i++) {
      this.children[i].finishIfStarted();
    }
    this.writeFinishMessage();
    this.isFinished = true;
  }
};

Node.prototype.writeStartMessage = function () {
  if (this.parentNode && this.parentNode.id === 1) {
    this.parentNode.writeStartMessage();
  }
  var text = this.getStartMessage();
  this.tree.write(text + '\n');
};

Node.prototype.writeFinishMessage = function () {
  var text = this.getFinishMessage();
  this.tree.write(text + '\n');
  this.isFinished = true;
};

Node.prototype.getStartMessage = function () {
  var text = "##teamcity[";
  text += this.getStartCommandName();
  text += " nodeId='" + this.id;
  var parentNodeId = this.parentNode ? this.parentNode.id : 0;
  text += "' parentNodeId='" + parentNodeId;
  text += "' name='" + escapeStr(this.name);
  text += "']";
  return text;
};

Node.prototype.getFinishMessage = function () {
  var text = "##teamcity[" + this.getFinishCommandName();
  text += " nodeId='" + this.id + "'";
  var extraParameters = this.getExtraFinishMessageParameters();
  if (extraParameters) {
    text += extraParameters;
  }
  text += "]";
  return text;
};

function TestSuiteNode(tree, id, parentNode, name, type) {
  Node.call(this, tree, id, parentNode, name, type);
  this.children = [];
  this.lookupMap = {};
}

inherit(TestSuiteNode, Node);

TestSuiteNode.prototype.getStartCommandName = function () {
  return 'testSuiteStarted';
};

TestSuiteNode.prototype.getFinishCommandName = function () {
  return 'testSuiteFinished';
};

TestSuiteNode.prototype.addChild = function (childName, isChildSuite) {
  if (this.isFinished) {
    throw Error("Child node could be created for finished node!");
  }
  var childId = this.tree.nextId++;
  var child;
  if (isChildSuite) {
    child = new TestSuiteNode(this.tree, childId, this, childName, '<unknown>');
  }
  else {
    child = new TestNode(this.tree, childId, this, childName, '<unknown>');
  }
  this.children.push(child);
  return child;
};


function TestNode(tree, id, parentNode, name, type) {
  Node.call(this, tree, id, parentNode, name, type);
}

inherit(TestNode, Node);

/**
 * @param {Number} status test status
 * 0 = success
 * 1 = skipped
 * 2 = failed
 * @param {Number} duration test duration is ms
 * @param {String} failureMsg
 */
TestNode.prototype.setStatus = function (status, duration, failureMsg) {
  this.status = status;
  this.duration = duration;
  this.failureMsg = failureMsg;
};

TestNode.prototype.getStartCommandName = function () {
  return 'testStarted';
};

TestNode.prototype.getFinishCommandName = function () {
  switch (this.status) {
    case 0:
      return 'testFinished';
    case 1:
      return 'testFailed';
    case 2:
      return 'testFailed';
    default:
      throw Error("Unexpected status: " + JSON.stringify(this.status));
  }
};

TestNode.prototype.getExtraFinishMessageParameters = function () {
  var params = '';
  if (typeof this.duration === 'number') {
    params += " duration='" + this.duration + "'";
  }
  if (this.failureMsg) {
    params += " message='" + escapeStr(this.failureMsg) + "'";
  }
  return params.length === 0 ? null : params;
};


var escapeCharCode = (function () {
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

function isEscapingNeeded(str) {
  var len = str.length;
  for (var i = 0; i < len; i++) {
    if (escapeCharCode(str.charCodeAt(i))) {
      return true;
    }
  }
  return false;
}

function escapeStr(str) {
  if (!isEscapingNeeded(str)) {
    return str;
  }
  var res = '',
    len = str.length;
  for (var i = 0; i < len.length; i++) {
    var escaped = escapeCharCode(str.charCodeAt(i));
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

module.exports = Tree;
