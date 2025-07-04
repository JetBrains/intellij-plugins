module.exports = function (config) {
  config.set({
    basePath: './',
    files: [
      './src/*.js',
      './tests/*.js'

    ],

    autoWatch: false,
    singleRun: true,
    colors: true,

    logLevel: config.LOG_INFO,

    frameworks: ['jasmine'],


    browsers: ['ChromeHeadless'],

    preprocessors: {
      './src/*.js': 'coverage',
      './tests/*.js': 'coverage'
    },

    reporters: ['junit', 'coverage', 'progress'],

    junitReporter: {
      outputDir: 'junit/',
      outputFile: 'TESTS-xunit.xml',
      useBrowserName: false
    },

    coverageReporter: {
      type: 'lcov',
      dir: 'coverage/',
      subdir: '.'
    }

  });
}