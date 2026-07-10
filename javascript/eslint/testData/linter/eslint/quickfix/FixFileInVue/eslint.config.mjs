import vueParser from "vue-eslint-parser";

export default [
  {
    files: ["**/*.vue"],
    languageOptions: {
      parser: vueParser,
    },
    rules: {
      "quotes": ["error", "single"],
      "semi": ["error", "always"],
    },
  },
];
