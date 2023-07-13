import { CNode } from 'css-render';
interface FadeDownTransitionOptions {
    name?: string;
    fromOffset?: string;
    enterDuration?: string;
    leaveDuration?: string;
    enterCubicBezier?: string;
    leaveCubicBezier?: string;
}
export declare function fadeDownTransition({ name, fromOffset, enterDuration, leaveDuration, enterCubicBezier, leaveCubicBezier }?: FadeDownTransitionOptions): CNode[];
export {};
