export default [
  {
    files: ["**/*.jsx"],
    languageOptions: {
      sourceType: "module",
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
    rules: {
      "jsx-quotes": "error",
    },
  },
];
