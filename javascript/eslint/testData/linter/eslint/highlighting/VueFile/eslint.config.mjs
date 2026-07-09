import vueParser from "vue-eslint-parser";

export default [
  {
    files: ["**/*.vue"],
    languageOptions: { parser: vueParser },
    rules: {
      "no-console": "error",
    },
  },
];
