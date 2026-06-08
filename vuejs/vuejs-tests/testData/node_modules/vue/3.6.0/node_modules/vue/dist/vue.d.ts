import { CompilerOptions } from "@vue/compiler-dom";
import { RenderFunction, defineAsyncComponent as defineVaporAsyncComponent$1 } from "@vue/runtime-dom";
import { VaporSlot, defineVaporAsyncComponent, withAsyncContext } from "@vue/runtime-vapor";
export * from "@vue/runtime-dom";
export * from "@vue/runtime-vapor";

//#region \0rolldown/runtime.js
//#endregion
//#region temp/packages/vue/src/index.d.ts
export declare function compileToFunction(template: string | HTMLElement, options?: CompilerOptions): RenderFunction;
//#endregion
export { type VaporSlot, compileToFunction as compile, defineVaporAsyncComponent, withAsyncContext };