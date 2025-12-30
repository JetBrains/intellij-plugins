// based on https://github.com/vuejs/language-tools/blob/5607f45835ab85e0b5a0747614a4ed9989a28cec/extensions/vscode/scripts/build.js

const path = require('path');
const fs = require('fs');

require('esbuild').build({
	entryPoints: {
		"./index": './src',
	},
	outdir: '../../gen-resources/angular-service/node_modules/ws-typescript-angular-plugin',
	bundle: true,
	external: [
    "tsc-ide-plugin",
  ],
	format: 'cjs',
	platform: 'node',
  target: 'es2015',
	tsconfig: './tsconfig.json',
	define: { 'process.env.NODE_ENV': '"production"' },
  sourcemap: "linked",
	minify: process.argv.includes('--minify'),
  metafile: process.argv.includes('--metafile'),
	plugins: [
		{
			name: 'umd2esm',
			setup(build) {
				build.onResolve({ filter: /^(vscode-.*-languageservice|jsonc-parser)/ }, args => {
					const pathUmdMay = require.resolve(args.path, { paths: [args.resolveDir] })
					// Call twice the replace is to solve the problem of the path in Windows
					const pathEsm = pathUmdMay.replace('/umd/', '/esm/').replace('\\umd\\', '\\esm\\')
					return { path: pathEsm }
				})
			},
		},
	],
});