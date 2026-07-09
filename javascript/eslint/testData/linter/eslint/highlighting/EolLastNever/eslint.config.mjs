export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    rules: {
      "eol-last": ["error", "never"],
      "no-magic-numbers": "error",
    },
  },
];
