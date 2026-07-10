import html from "eslint-plugin-html";

export default [
  {
    rules: {
      "no-multiple-empty-lines": "error",
      "one-var": ["error", "never"],
      "semi": ["error", "always"],
      "quotes": ["error", "single"],
    },
  },
  {
    files: ["**/*.html"],
    plugins: { html },
  },
];
