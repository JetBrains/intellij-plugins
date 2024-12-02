module.exports = function(config) {
  config.set({
    frameworks: ['jasmine'],
    files: ['./src/**/*.spec.js'],
    browsers: ['Firefox'],
  })
}
