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
