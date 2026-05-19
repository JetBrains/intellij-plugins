// mimic behavior of 'neutrino' module.
const neutrino = require(process.cwd() + '/.neutrinorc.js')

module.exports = {
  rules: {
    "semi": ["error", "never"]
  }
}
