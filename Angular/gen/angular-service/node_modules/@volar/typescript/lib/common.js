"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.fileLanguageIdProviderPlugin = void 0;
exports.fileLanguageIdProviderPlugin = {
    getLanguageId(scriptId) {
        const ext = scriptId.split('.').pop();
        switch (ext) {
            case 'js': return 'javascript';
            case 'cjs': return 'javascript';
            case 'mjs': return 'javascript';
            case 'ts': return 'typescript';
            case 'cts': return 'typescript';
            case 'mts': return 'typescript';
            case 'jsx': return 'javascriptreact';
            case 'tsx': return 'typescriptreact';
            case 'json': return 'json';
        }
    },
};
//# sourceMappingURL=common.js.map