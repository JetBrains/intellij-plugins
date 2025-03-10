// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
module.exports = config => {
  config.set({
    basePath: '',
    frameworks: ['qunit'],
    files: ['./src/*.spec.js'],
    port: 9876,
    colors: true,
    autoWatch: false,
    browsers: ['ChromeHeadless'],
    singleRun: true,
  })
}
