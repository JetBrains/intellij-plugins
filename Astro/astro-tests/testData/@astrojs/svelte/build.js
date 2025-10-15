const path = require('path');
const fs = require('fs');
const esbuild = require('esbuild');

const targetPackage = '@astrojs/svelte';

const pkgPath = require.resolve(`${targetPackage}/package.json`);
const theirPkg = require(pkgPath);
const pkgDir = path.dirname(pkgPath);

const entries = {
  // '.': path.join(pkgDir, 'dist/index.js'),
  // './client.js': path.join(pkgDir, 'dist/client.svelte.js'),
  // './server.js': path.join(pkgDir, 'dist/server.js'),
  './editor': path.join(pkgDir, 'dist/editor.cjs'),
};

const outDir = path.resolve(__dirname, 'dist');
fs.rmSync(outDir, {recursive: true, force: true});
fs.mkdirSync(outDir, {recursive: true});

const externals = [
  'lightningcss',
  'fsevents',
];

function externalizeNativeNode() {
  return {
    name: 'externalize-native-node',
    setup(build) {
      build.onResolve({filter: /\.node$/}, (args) => ({path: args.path, external: true}));
    },
  };
}

async function buildEntry(entryPath, outFileName, format) {
  const outfile = path.join(outDir, outFileName);
  await esbuild.build({
    entryPoints: [entryPath],
    outfile,
    bundle: true,
    platform: 'node',
    target: 'node18',
    format, // 'esm' or 'cjs'
    minify: process.argv.includes('--minify'),
    sourcemap: false,
    define: {'process.env.NODE_ENV': '"production"'},
    external: externals,
    plugins: [externalizeNativeNode()],
  });
  return outfile;
}

async function buildAll() {
  console.log(`[build] Bundling ${targetPackage}@${theirPkg.version}`);

  const builtFiles = [];

  for (const [key, entryPath] of Object.entries(entries)) {
    if (key === '.') {
      console.log(` - ${key} (esm)`);
      builtFiles.push({key, format: 'esm', outfile: await buildEntry(entryPath, 'index.mjs', 'esm')});

      console.log(` - ${key} (cjs)`);
      builtFiles.push({key, format: 'cjs', outfile: await buildEntry(entryPath, 'index.cjs', 'cjs')});
      continue;
    }

    if (key === './client.js') {
      console.log(` - ${key} (esm) → client.svelte.mjs`);
      builtFiles.push({
        key,
        format: 'esm',
        outfile: await buildEntry(entryPath, 'client.svelte.js', 'esm'),
      });
      continue;
    }

    if (key === './server.js') {
      console.log(` - ${key} (esm) → server.mjs`);
      builtFiles.push({
        key,
        format: 'esm',
        outfile: await buildEntry(entryPath, 'server.js', 'esm'),
      });
      continue;
    }

    if (key === './editor') {
      console.log(` - ${key} (cjs) → editor.cjs`);
      builtFiles.push({
        key,
        format: 'cjs',
        outfile: await buildEntry(entryPath, 'editor.cjs', 'cjs'),
      });
      continue;
    }
  }
}

buildAll().catch((err) => {
  console.error(err);
  process.exit(1);
});