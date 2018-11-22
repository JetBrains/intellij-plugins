/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var intellijUtil = require('./intellijUtil.js')

exports.startBrowserTracking = function (globalEmitter) {
  var oldConnectionId2BrowserObj = {};
  globalEmitter.on('browsers_change', function(capturedBrowsers) {
    if (!capturedBrowsers.forEach) {
      // filter out events from Browser object
      return;
    }
    var newConnectionId2BrowserObj = {};
    var proceed = true;
    capturedBrowsers.forEach(function(newBrowser) {
      if (!newBrowser.id || !newBrowser.name || newBrowser.id === newBrowser.name) {
        proceed = false;
      }
      newConnectionId2BrowserObj[newBrowser.id] = newBrowser;
    });
    if (proceed) {
      sendBrowserEvents('browserConnected', newConnectionId2BrowserObj, oldConnectionId2BrowserObj, true);
      sendBrowserEvents('browserDisconnected', oldConnectionId2BrowserObj, newConnectionId2BrowserObj, false);
      oldConnectionId2BrowserObj = newConnectionId2BrowserObj;
    }
  });
  globalEmitter.on('load_error', function (type, name) {
    if (type === 'launcher') {
      intellijUtil.sendIntellijEvent('browserCapturingFailed', {browserLauncherName: name});
    }
  });
  globalEmitter.on('browser_process_failure', function (browserLauncher) {
    intellijUtil.sendIntellijEvent('browserCapturingFailed', {browserLauncherName: browserLauncher.name});
  });
}

function sendBrowserEvents(eventType, connectionId2BrowserObjA, connectionId2BrowserObjB, addAutoCapturingInfo) {
  for (var connectionId in connectionId2BrowserObjA) {
    if (connectionId2BrowserObjA.hasOwnProperty(connectionId)) {
      if (!connectionId2BrowserObjB.hasOwnProperty(connectionId)) {
        var browser = connectionId2BrowserObjA[connectionId];
        var event = {id: connectionId, name: browser.name};
        if (addAutoCapturingInfo) {
          event.isAutoCaptured = isAutoCapturedBrowser(browser);
        }
        intellijUtil.sendIntellijEvent(eventType, event);
      }
    }
  }
}

function isAutoCapturedBrowser(browser) {
  if (browser.launchId != null) {
    return true;
  }
  var idStr = browser.id;
  if (intellijUtil.isString(idStr)) {
    return /^\d+$/.test(idStr);
  }
  return false;
}
