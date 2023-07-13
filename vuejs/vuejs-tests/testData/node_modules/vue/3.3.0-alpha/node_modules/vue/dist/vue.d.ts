import { CompilerOptions } from '@vue/compiler-dom';
import { RenderFunction } from '@vue/runtime-dom';
export * from '@vue/runtime-dom';

declare function compileToFunction(template: string | HTMLElement, options?: CompilerOptions): RenderFunction;

export { compileToFunction as compile };
