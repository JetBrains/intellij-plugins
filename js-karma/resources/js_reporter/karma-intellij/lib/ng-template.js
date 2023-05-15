// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

// https://github.com/angular/angular-cli/blob/main/packages/schematics/angular/config/files/karma.conf.js.template
// https://github.com/angular/angular-cli/blob/8095268fa4e06c70f2f11323cff648fc6d4aba7d/packages/angular_devkit/build_angular/src/builders/karma/index.ts#L188-L237

function getBuiltInKarmaConfig(config) {
  const workspaceRoot = process.cwd();
  const workspaceRootRequire = require('module').createRequire(workspaceRoot + '/');
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      'karma-jasmine',
      'karma-chrome-launcher',
      'karma-jasmine-html-reporter',
      'karma-coverage',
      '@angular-devkit/build-angular/plugins/karma',
    ].map((p) => workspaceRootRequire(p)),
    client: {
      clearContext: false, // leave Jasmine Spec Runner output visible in browser
    },
    jasmineHtmlReporter: {
      suppressAll: true, // removes the duplicated traces
    },
    coverageReporter: {
      dir: require('path').join(workspaceRoot, 'coverage'),
      subdir: '.',
      reporters: [{ type: 'html' }, { type: 'text-summary' }],
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['Chrome'],
    customLaunchers: {
      // Chrome configured to run in a bazel sandbox.
      // Disable the use of the gpu and `/dev/shm` because it causes Chrome to
      // crash on some environments.
      // See:
      //   https://github.com/puppeteer/puppeteer/blob/v1.0.0/docs/troubleshooting.md#tips
      //   https://stackoverflow.com/questions/50642308/webdriverexception-unknown-error-devtoolsactiveport-file-doesnt-exist-while-t
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--headless', '--disable-gpu', '--disable-dev-shm-usage'],
      },
    },
    restartOnFileChange: true,
  });
}

exports.getBuiltInKarmaConfig = getBuiltInKarmaConfig;
