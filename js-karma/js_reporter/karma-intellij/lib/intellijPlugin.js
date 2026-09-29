/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var IntellijReporter = require('./intellijReporter')
  , IntellijCoverageReporter = require('./intellijCoverageReporter');

var extensions = {};
extensions['reporter:' + IntellijReporter.reporterName] = ['type', IntellijReporter];
extensions['reporter:' + IntellijCoverageReporter.reporterName] = ['type', IntellijCoverageReporter];

module.exports = extensions;
