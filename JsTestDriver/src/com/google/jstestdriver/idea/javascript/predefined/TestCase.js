/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


function TestCase(testCaseName, opt_proto, opt_type) {
  var testCaseClass = function() {
  };

  if (opt_proto) {
    testCaseClass.prototype = opt_proto;
  }
  if (typeof testCaseClass.prototype.setUp == 'undefined') {
    testCaseClass.prototype.setUp = function() {
    };
  }
  if (!testCaseClass.prototype.hasOwnProperty('toString')) {
    testCaseClass.prototype.toString = function() {
      return "TestCase(" + testCaseName + ")";
    };
  }
  if (typeof testCaseClass.prototype.tearDown == 'undefined') {
    testCaseClass.prototype.tearDown = function() {
    };
  }

  return testCaseClass;
}


/**
 * Defines an asynchronous test case.
 * @param {string} testCaseName The name of the test case.
 * @param {Object} opt_proto An optional prototype.
 * @return {Function} Base function that represents the asyncronous test case
 *     class.
 */
function AsyncTestCase(testCaseName, opt_proto) {
  return TestCase(testCaseName, opt_proto, 'async');
}


/**
 * A TestCase that will only be executed when a certain condition is true.
 * @param {string} The name of the TestCase.
 * @param {function():boolean} A function that indicates if this case should be
 *     run.
 * @param {Object} opt_proto An optional prototype for the test case class.
 * @param {Object} opt_type Either DEFAULT_TYPE or ASYNC_TYPE.
 * @return {Function} Base function that represents the TestCase class.
 */
function ConditionalTestCase(testCaseName, condition, opt_proto, opt_type) {
  if (condition()) {
    return TestCase(testCaseName, opt_proto, opt_type);
  }
  return function(){};
}


/**
 * An AsyncTestCase that will only be executed when a certain condition is true.
 * @param {String} The name of the AsyncTestCase.
 * @param {function():boolean} A function that indicates if this case should be
 *     run.
 * @param {Object} opt_proto An optional prototype for the test case class.
 * @return {Function} Base function that represents the TestCase class.
 */
function ConditionalAsyncTestCase(testCaseName, condition, opt_proto) {
  return ConditionalTestCase(testCaseName, condition, opt_proto, 'async');
}
