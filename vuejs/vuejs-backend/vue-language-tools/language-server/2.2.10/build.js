// based on https://github.com/vuejs/language-tools/blob/5607f45835ab85e0b5a0747614a4ed9989a28cec/extensions/vscode/scripts/build.js

const path = require('path');
const fs = require('fs');

// .js is here, so it's easier to Find in Files
const packageRelativePath = "bin/vue-language-server.js"
  .replace(".js", "");

const ownPackageJson = require('./package.json');
const languageServerPackage = ownPackageJson.name;

const theirPackageJson = require(`./node_modules/${languageServerPackage}/package.json`);

if (ownPackageJson.version !== theirPackageJson.version) {
  throw new Error(`Make sure that the version in package.json matches the version of official server distribution (${theirPackageJson.version})`);
}

require('esbuild').build({
	entryPoints: {
		[packageRelativePath]: `./node_modules/${languageServerPackage}/${packageRelativePath}`,
	},
	outdir: '.',
	bundle: true,
	format: 'cjs',
	platform: 'node',
	target: 'es2015',
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