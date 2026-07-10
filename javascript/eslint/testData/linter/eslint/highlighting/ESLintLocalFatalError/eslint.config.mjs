export default [
  { ignores: ["**/eslint.config.mjs"] },
  {
    languageOptions: { ecmaVersion: 5, sourceType: "script" },
    rules: {
      "no-console": "error",
      "no-debugger": "warn",
      "curly": "off",
    },
  },
];
