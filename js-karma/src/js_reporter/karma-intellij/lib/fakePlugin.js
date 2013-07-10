// If 'karma-intellij' plugin is installed near 'karma', it can be loaded as a result of 'karma-*' expansion.
// This can lead to unexpected results, because 'karma-intellij' plugin is also added explicitly in 'intellij.conf.js'.
// To prevent it, 'karma-intellij' plugin located near 'karma' exports empty extension list.
module.exports = {};
