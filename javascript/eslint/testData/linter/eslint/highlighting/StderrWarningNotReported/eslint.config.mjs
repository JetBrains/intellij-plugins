// A rule that logs a warning without a stack, as eslint-plugin-react does for a missing React version (WEB-78300).
const warningRule = {
  create() {
    return {
      Program() {
        console.error("Warning: React version not specified in eslint-plugin-react settings.");
      },
    };
  },
};

export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    plugins: { local: { rules: { warning: warningRule } } },
    rules: {
      "local/warning": "error",
      "no-debugger": "error",
    },
  },
];
