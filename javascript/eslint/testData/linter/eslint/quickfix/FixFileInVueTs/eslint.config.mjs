import vueParser from "vue-eslint-parser";
import tsParser from "@typescript-eslint/parser";

export default [
  {
    files: ["**/*.vue"],
    languageOptions: {
      parser: vueParser,
      parserOptions: { parser: tsParser },
    },
    rules: {
      "quotes": ["error", "single"],
      "semi": ["error", "always"],
    },
  },
];
