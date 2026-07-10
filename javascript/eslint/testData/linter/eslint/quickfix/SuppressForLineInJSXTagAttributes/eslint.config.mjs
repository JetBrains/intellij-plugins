import react from "eslint-plugin-react";

export default [
  {
    files: ["**/*.jsx"],
    plugins: { react },
    languageOptions: {
      sourceType: "module",
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
    rules: {
      "quotes": ["error", "single"],
      "react/jsx-curly-brace-presence": "error",
    },
  },
];
