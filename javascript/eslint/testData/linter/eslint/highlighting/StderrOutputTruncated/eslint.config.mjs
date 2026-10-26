// A rule that logs an exception and then a line of 70000 characters (WEB-78300). The capture keeps the first 64 KiB.
const chattyRule = {
  create() {
    return {
      Program() {
        console.error(new Error("ENOENT: no such file or directory"));
        console.error("x".repeat(70000));
      },
    };
  },
};

export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    plugins: { local: { rules: { chatty: chattyRule } } },
    rules: {
      "local/chatty": "error",
      "no-debugger": "error",
    },
  },
];
