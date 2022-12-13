const path = require('path')
const CopyPlugin = require('copy-webpack-plugin')

const root = path.resolve(__dirname);
const dist = path.resolve(root, "../gen/language-server")

module.exports = {
    mode: 'production',
    target: 'node',
    devtool: false,
    entry: path.resolve(__dirname, './index.js'),
    output: {
        path: dist,
        filename: 'prisma-language-server.js',
        clean: true,
    },
    plugins: [
        new CopyPlugin({
            patterns: [
                {
                    from: './node_modules/@prisma/prisma-fmt-wasm/**/*.wasm',
                    to: '[name][ext]',
                },
            ],
        }),
    ],
}
