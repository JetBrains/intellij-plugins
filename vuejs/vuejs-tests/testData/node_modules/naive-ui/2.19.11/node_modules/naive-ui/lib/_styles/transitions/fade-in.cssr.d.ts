import { CNode } from 'css-render';
interface FadeInTransitionOptions {
    name?: string;
    enterDuration?: string;
    leaveDuration?: string;
    enterCubicBezier?: string;
    leaveCubicBezier?: string;
}
export default function ({ name, enterDuration, leaveDuration, enterCubicBezier, leaveCubicBezier }?: FadeInTransitionOptions): CNode[];
export {};
