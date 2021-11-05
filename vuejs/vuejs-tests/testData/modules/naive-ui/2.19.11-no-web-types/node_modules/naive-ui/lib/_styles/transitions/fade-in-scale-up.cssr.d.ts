import { CNode } from 'css-render';
interface FadeInScaleUpTransitionOptions {
    transformOrigin?: string;
    duration?: string;
    enterScale?: string;
    originalTransform?: string;
    originalTransition?: string;
}
export default function ({ transformOrigin, duration, enterScale, originalTransform, originalTransition }?: FadeInScaleUpTransitionOptions): CNode[];
export {};
