// A rule that ends the service process, as a crash of the runtime does (WEB-78300). The colored line imitates a crash report.
const exitingRule = {
  create() {
    return {
      Program() {
        console.error("\u001B[31mpanic: the runtime failed\u001B[39m");
        process.exit(3);
      },
    };
  },
};

export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    plugins: { local: { rules: { exiting: exitingRule } } },
    rules: {
      "local/exiting": "error",
    },
  },
];
