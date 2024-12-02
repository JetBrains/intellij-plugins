module.exports = function(config) {
  config.set({
    client: {
      mocha: {
        ui: 'tdd'
      }
    },
    frameworks: ['mocha', 'expect'],
    files: [
      "./src/**/*.spec.js"
    ],
    reporters: ['mocha'],
    browsers: ['Firefox']
  })
}
