import { readFileSync } from 'node:fs';
import { getDefaultContext, instantiateNapiModuleSync, WASI } from '@napi-rs/wasm-runtime';

const wasi = new WASI({
	version: 'preview1',
	env: process.env,
});
const memory = new WebAssembly.Memory({
	initial: 4000,
	maximum: 65536,
	shared: true,
});
const { napiModule } = instantiateNapiModuleSync(
	readFileSync(new URL('./astro2tsx.wasm32-wasi.wasm', import.meta.url)),
	{
		context: getDefaultContext(),
		wasi,
		overwriteImports(importObject) {
			importObject.env = {
				...importObject.env,
				...importObject.napi,
				...importObject.emnapi,
				memory,
			};
			return importObject;
		},
		beforeInit({ instance }) {
			for (const name of Object.keys(instance.exports)) {
				if (name.startsWith('__napi_register__')) instance.exports[name]();
			}
		},
	},
);

const {
	AstroFrontmatterStatus,
	convertToTsx,
	DiagnosticSeverity,
	ExtractedScriptType,
	ExtractedStyleType,
} = napiModule.exports;

export {
	AstroFrontmatterStatus,
	convertToTsx,
	DiagnosticSeverity,
	ExtractedScriptType,
	ExtractedStyleType,
};
