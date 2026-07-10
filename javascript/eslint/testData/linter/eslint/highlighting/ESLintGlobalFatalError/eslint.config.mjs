// Importing a nonexistent module makes the flat config fail to load -> a config-load fatal error,
// surfaced by the IDE as a file-level ESLint error (the flat-config analog of the old eslintrc
// "Failed to load parser 'babel'" scenario).
import config from "eslint-config-does-not-exist";

export default [config];
