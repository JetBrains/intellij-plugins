// A rule that prints what Node.js prints for a deprecation under --trace-deprecation (WEB-78300). A real warning of a
// request arrives after its response, but it lands in the window of a concurrent request. The stack must not make it a failure.
const deprecationRule = {
  create() {
    return {
      Program() {
        console.error(
          "(node:12345) [DEP0040] DeprecationWarning: The `punycode` module is deprecated. Please use a userland alternative instead.\n" +
          "    at node:internal/deps/punycode:1:1",
        );
      },
    };
  },
};

export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    plugins: { local: { rules: { deprecation: deprecationRule } } },
    rules: {
      "local/deprecation": "error",
      "no-debugger": "error",
    },
  },
];
