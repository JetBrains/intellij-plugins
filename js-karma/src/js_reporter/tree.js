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
  this.configFileNode = new TestSuiteNode(this, 1, null, path.basename(configFilePath), 'config', 'locationHint');
  this.write = write;
  this.nextId = 2;
}

/**
 * Node class is a base class for TestSuiteNode and TestNode classes.
 *
 * @param {Tree} tree test tree
 * @param {Number} id this node ID. It should be unique among all node IDs that belong to the same tree.
 * @param {Node} parentNode parent node
 * @param {String} name node name (it could be a suite/spec name)
 * @param {String} type node type (e.g. 'config', 'browser')
 * @param {String} locationHint string that is used by IDE to navigate to the source code
 * @constructor
 */
function Node(tree, id, parentNode, name, type, locationHint) {
  this.tree = tree;
  this.id = id;
  this.parentNode = parentNode;
  this.name = name;
  this.type = type;
  this.locationHint = locationHint;
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
  if (this.type != null) {
    text += "' nodeType='" + this.type
  }
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

// class TestSuiteNode extends Node

function TestSuiteNode(tree, id, parentNode, name, type, locationHint) {
  Node.call(this, tree, id, parentNode, name, type, locationHint);
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


/**
 * TestNode class that represents a spec node.
 *
 * @param {Tree} tree test tree
 * @param {Number} id this node ID. It should be unique among all node IDs that belong to the same tree.
 * @param {TestSuiteNode} parentNode parent node
 * @param {String} name node name (for example, it could be a spec name)
 * @param {String} type node type (e.g. 'config', 'browser')
 * @constructor
 */
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



module.exports = Tree;
