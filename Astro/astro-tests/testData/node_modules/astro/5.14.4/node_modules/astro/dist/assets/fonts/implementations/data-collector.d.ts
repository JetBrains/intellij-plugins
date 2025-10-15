import type { DataCollector } from '../definitions.js';
import type { CreateUrlProxyParams } from '../types.js';
export declare function createDataCollector({ hasUrl, saveUrl, savePreload, saveFontData, }: Pick<CreateUrlProxyParams, 'hasUrl' | 'saveUrl' | 'savePreload' | 'saveFontData'>): DataCollector;
