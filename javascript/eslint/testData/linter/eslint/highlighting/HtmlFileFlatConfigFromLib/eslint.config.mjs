import html from "@html-eslint/eslint-plugin";

export default [
    {
        ...html.configs["flat/recommended"],

        files: ["**/*.html"],
    },
];
