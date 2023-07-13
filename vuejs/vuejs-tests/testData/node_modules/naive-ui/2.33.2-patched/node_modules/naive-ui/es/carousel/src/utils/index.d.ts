import { Size } from '../interface';
export * from './duplicatedLogic';
export * from './event';
export declare function calculateSize(element: HTMLElement, innerOnly?: boolean): Size;
export declare function clampValue(value: number, min: number, max: number): number;
export declare function resolveSpeed(value?: string | number): number;
