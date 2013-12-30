var path = require('path')
  , intellijUtil = require('./intellijUtil.js');

function inherit(child, parent) {
  function F() {
    this.constructor = child;
  }

  F.prototype = parent.prototype;
  child.prototype = new F();
  return child;
}


function Tree(configFilePath, write) {
  this.configFileNode = new TestSuiteNode(this, 1, null, path.basename(configFilePath), 'config', configFilePath);
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
 * @param {String} locationHint string that is used by IDE to navigate to the definition of the node
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
  writeStartMessageIfNeeded(this.parentNode);
  var text = this.getStartMessage();
  this.tree.write(text + '\n');
  this.isStartMessageWritten = true;
};

// hack
function writeStartMessageIfNeeded(node) {
  if (node && node.id === 1 && !node.isStartMessageWritten) {
    node.writeStartMessage();
  }
}

Node.prototype.writeFinishMessage = function () {
  writeStartMessageIfNeeded(this);
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
  text += "' name='" + intellijUtil.attributeValueEscape(this.name);
  if (this.type != null) {
    text += "' nodeType='" + this.type;
    if (this.locationHint != null) {
      text += "' locationHint='" + intellijUtil.attributeValueEscape(this.type + '://' + this.locationHint);
    }
  }
  text += "']";
  return text;
};

Node.prototype.getFinishMessage = function () {
  var text = '##teamcity[' + this.getFinishCommandName();
  text += " nodeId='" + this.id + "'";
  var extraParameters = this.getExtraFinishMessageParameters();
  if (extraParameters) {
    text += extraParameters;
  }
  text += ']';
  return text;
};

/**
 * TestSuiteNode child of Node class. Represents a suite node.
 *
 * @param {Tree} tree test tree
 * @param {Number} id this node's ID. It should be unique among all node IDs that belong to the same tree.
 * @param {TestSuiteNode} parentNode parent node
 * @param {String} name node name (e.g. config file name / browser name / suite name)
 * @param {String} type node type (e.g. 'config', 'browser')
 * @param {String} locationHint navigation info
 * @constructor
 */
function TestSuiteNode(tree, id, parentNode, name, type, locationHint) {
  Node.call(this, tree, id, parentNode, name, type, locationHint);
  this.children = [];
  this.lookupMap = {};
}

inherit(TestSuiteNode, Node);

/**
 * Returns child node by its name.
 * @param childKey unique string that identifies this node (it can be node's name)
 * @returns {?Node} child node (null, if no child node with such name found)
 */
TestSuiteNode.prototype.findChildNodeByKey = function(childKey) {
  if (Object.prototype.hasOwnProperty.call(this.lookupMap, childKey)) {
    return this.lookupMap[childKey];
  }
  return null;
};

TestSuiteNode.prototype.getStartCommandName = function () {
  return 'testSuiteStarted';
};

TestSuiteNode.prototype.getFinishCommandName = function () {
  return 'testSuiteFinished';
};

/**
 *
 * @param {String} childName node name (e.g. browser name / suite name / spec name)
 * @param {Boolean} isChildSuite true if child node can have children
 * @param {String} nodeType child node type (e.g. 'config', 'browser')
 * @param {String} locationHint navigation info
 * @returns {TestSuiteNode | TestNode}
 */
TestSuiteNode.prototype.addChild = function (childName, isChildSuite, nodeType, locationHint) {
  return this.addChildWithKey(childName, childName, isChildSuite, nodeType, locationHint);
};

/**
 *
 * @param {String} childKey unique string that identifies this node (sometimes childName isn't enough)
 * @param {String} childName node name (e.g. browser name / suite name / spec name)
 * @param {Boolean} isChildSuite true if child node can have children
 * @param {String} nodeType child node type (e.g. 'config', 'browser')
 * @param {String} locationHint navigation info
 * @returns {TestSuiteNode | TestNode}
 */
TestSuiteNode.prototype.addChildWithKey = function (childKey, childName, isChildSuite, nodeType, locationHint) {
  if (this.isFinished) {
    throw Error('Child node could be created for finished node!');
  }
  var childId = this.tree.nextId++;
  var child;
  if (isChildSuite) {
    child = new TestSuiteNode(this.tree, childId, this, childName, nodeType, locationHint);
  }
  else {
    child = new TestNode(this.tree, childId, this, childName, nodeType, locationHint);
  }
  this.children.push(child);
  if (isChildSuite) {
    this.lookupMap[childKey] = child;
  }
  return child;
};


/**
 * TestNode class that represents a spec node.
 *
 * @param {Tree} tree test tree
 * @param {Number} id this node ID. It should be unique among all node IDs that belong to the same tree.
 * @param {TestSuiteNode} parentNode parent node
 * @param {String} name node name (spec name)
 * @param {String} type node type (e.g. 'config', 'browser')
 * @param {String} locationHint navigation info
 * @constructor
 */
function TestNode(tree, id, parentNode, name, type, locationHint) {
  Node.call(this, tree, id, parentNode, name, type, locationHint);
}

inherit(TestNode, Node);

/**
 * @param {Number} status test status
 * 0 = success
 * 1 = skipped
 * 2 = failed
 * 3 = error
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
      return 'testIgnored';
    case 2:
      return 'testFailed';
    case 3:
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
  if (this.status === 3) {
    params += " error='yes'";
  }
  if (this.failureMsg) {
    params += " message='" + intellijUtil.attributeValueEscape(this.failureMsg) + "'";
  }
  return params.length === 0 ? null : params;
};


module.exports = Tree;
