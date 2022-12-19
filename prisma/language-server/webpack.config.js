const path = require('path')
const CopyPlugin = require('copy-webpack-plugin')

const root = path.resolve(__dirname)
const dist = path.resolve(root, "../gen/language-server")

module.exports = {
  mode: 'production',
  target: 'node',
  devtool: false,
  entry: {
    "prisma-language-server": path.resolve(__dirname, './prisma-language-server.js'),
    "prisma-fmt": path.resolve(__dirname, './prisma-fmt.js'),
  },
  output: {
    path: dist,
    filename: '[name].js',
    clean: true,
  },
  plugins: [
    new CopyPlugin(
      {
        patterns: [
          {
            from: './node_modules/@prisma/prisma-fmt-wasm/**/*.wasm',
            to: '[name][ext]',
          },
        ],
      }
    ),
  ],
}
