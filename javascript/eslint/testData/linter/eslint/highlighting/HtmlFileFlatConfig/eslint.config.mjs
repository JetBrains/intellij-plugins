import html from "@html-eslint/eslint-plugin";
import {defineConfig} from "eslint/config";

const linterOptions = {
	"reportUnusedDisableDirectives": "error",
	"reportUnusedInlineConfigs": "error"
};

export default defineConfig([{
	files: ["./**/*.html", "./**/*.txt"],
	plugins: {
		html
	},
	language: "html/html",
	rules: {
		"html/lowercase": "error",
		"html/no-duplicate-class": "error"
	}
}, {
	files: ["./**/*.mjs"],
	linterOptions,
	rules: {
		"camelcase": "error"
	}
}]);