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
            // yaml-language-server 2.16.x requires prettier eagerly at module top-level, which crashes
            // the bundled server on startup because prettier is not shipped next to nodeServer.js.
            // Move the requires inside format() (as older versions did) so they are evaluated lazily,
            // only if YAML formatting is actually requested (it never is: the IDE disables LSP formatting).
            name: 'lazy-yaml-formatter-prettier',
            setup(build) {
                build.onLoad({filter: /yamlFormatter\.js$/}, args => {
                    let contents = fs.readFileSync(args.path, 'utf8');
                    const eager =
                        'const yamlPlugin = require("prettier/plugins/yaml");\n' +
                        'const estreePlugin = require("prettier/plugins/estree");\n' +
                        'const standalone_1 = require("prettier/standalone");\n';
                    const guard =
                        '        if (!this.formatterEnabled) {\n' +
                        '            return [];\n' +
                        '        }\n';
                    if (!contents.includes(eager) || !contents.includes(guard)) {
                        throw new Error('yamlFormatter.js layout changed; update lazy-yaml-formatter-prettier in build.js');
                    }
                    contents = contents
                        .replace(eager, '')
                        .replace(guard, guard +
                            '        const yamlPlugin = require("prettier/plugins/yaml");\n' +
                            '        const estreePlugin = require("prettier/plugins/estree");\n' +
                            '        const standalone_1 = require("prettier/standalone");\n');
                    return {contents, loader: 'js'};
                });
            },
        },
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
