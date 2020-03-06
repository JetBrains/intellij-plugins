import { InjectionToken } from '@angular/core';
import { HashMap, Translation } from './types';
export declare const TRANSLOCO_TRANSPILER: InjectionToken<{}>;
export interface TranslocoTranspiler {
    transpile(value: any, params: HashMap<any>, translation: HashMap): any;
}
export declare class DefaultTranspiler implements TranslocoTranspiler {
    transpile(value: any, params: HashMap<any>, translation: Translation): any;
}
