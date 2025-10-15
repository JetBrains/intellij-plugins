import type { ConsumableMap } from './types.js';
export declare function createGetFontData({ consumableMap }: {
    consumableMap?: ConsumableMap;
}): (cssVariable: string) => import("./types.js").FontData[];
