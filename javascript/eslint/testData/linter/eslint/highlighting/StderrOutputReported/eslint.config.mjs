// A rule that catches its own failure and only logs it, as eslint-plugin-exception-handling 1.6.0 does (WEB-78300).
const failingRule = {
  create() {
    return {
      Program() {
        console.error("Could not read file [object Object]");
        console.error(new Error("ENOENT: no such file or directory"));
      },
    };
  },
};

export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    plugins: { local: { rules: { failing: failingRule } } },
    rules: {
      "local/failing": "error",
      "no-debugger": "error",
    },
  },
];
