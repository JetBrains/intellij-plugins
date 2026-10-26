// The test starts the service with an option that Node.js rejects, so the process exits before it is ready (WEB-78300).
export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    rules: {
      "no-debugger": "error",
    },
  },
];
