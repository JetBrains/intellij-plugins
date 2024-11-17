// based on https://github.com/withastro/language-tools/blob/f1bdeabfc619074c9e38940b924f57c0e9018296/packages/vscode/scripts/build.mjs

const path = require('path');
const fs = require('fs');
const esbuild = require('esbuild');

// .js is here, so it's easier to Find in Files
const packageRelativePath = "bin/nodeServer.js"
    .replace(".js", "");

const ownPackageJson = require('./package.json');
const languageServerPackage = ownPackageJson.name;

const theirPackageJson = require(`./node_modules/${languageServerPackage}/package.json`);

if (ownPackageJson.version !== theirPackageJson.version) {
    throw new Error(`Make sure that the version in package.json matches the version of official server distribution (${theirPackageJson.version})`);
}

const vendoredDependencies = [
  `@astrojs/compiler`
];

esbuild.build({
    entryPoints: {
        [packageRelativePath]: `./node_modules/${languageServerPackage}/${packageRelativePath}`,
    },
    outdir: '.',
    bundle: true,
    external: [...vendoredDependencies, 'prettier', 'prettier-plugin-astro'],
    format: 'cjs',
    platform: 'node',
    target: 'es2015',
    define: {'process.env.NODE_ENV': '"production"'},
    sourcemap: "linked",
    minify: process.argv.includes('--minify'),
    metafile: process.argv.includes('--metafile'),
    plugins: [
        {
            name: 'umd2esm',
            setup(build) {
                build.onResolve({filter: /^(vscode-.*-languageservice|jsonc-parser)/}, args => {
                    const pathUmdMay = require.resolve(args.path, {paths: [args.resolveDir]})
                    // Call twice the replace is to solve the problem of the path in Windows
                    const pathEsm = pathUmdMay.replace('/umd/', '/esm/').replace('\\umd\\', '\\esm\\')
                    return {path: pathEsm}
                })
            },
        },
    ],
});

fs.rmSync('./bin/node_modules/', {recursive: true, force: true});

for (const vendoredDependency of vendoredDependencies) {
  fs.cpSync(`./node_modules/${vendoredDependency}`, `./bin/node_modules/${vendoredDependency}`, {recursive: true});
}
