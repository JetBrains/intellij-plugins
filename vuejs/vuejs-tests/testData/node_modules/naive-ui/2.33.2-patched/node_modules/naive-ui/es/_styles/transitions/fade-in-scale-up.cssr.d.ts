import { CNode } from 'css-render';
interface FadeInScaleUpTransitionOptions {
    transformOrigin?: string;
    duration?: string;
    enterScale?: string;
    originalTransform?: string;
    originalTransition?: string;
}
export declare function fadeInScaleUpTransition({ transformOrigin, duration, enterScale, originalTransform, originalTransition }?: FadeInScaleUpTransitionOptions): CNode[];
export {};
