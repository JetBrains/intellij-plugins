/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

// If 'karma-intellij' plugin is installed near 'karma', it can be loaded as a result of 'karma-*' expansion.
// This can lead to unexpected results, because 'karma-intellij' plugin is also added explicitly in 'intellij.conf.js'.
// To prevent it, 'karma-intellij' plugin located near 'karma' exports empty extension list.
module.exports = {};
