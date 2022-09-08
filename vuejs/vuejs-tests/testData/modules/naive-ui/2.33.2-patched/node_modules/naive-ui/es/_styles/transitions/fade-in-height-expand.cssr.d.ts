import { CNode } from 'css-render';
interface FadeInHeightExpandTransitionOption {
    overflow?: string;
    duration?: string;
    originalTransition?: string;
    leavingDelay?: string;
    foldPadding?: boolean;
    enterToProps?: Record<string, string | number> | undefined;
    leaveToProps?: Record<string, string | number> | undefined;
    reverse?: boolean;
}
export declare function fadeInHeightExpandTransition({ overflow, duration, originalTransition, leavingDelay, foldPadding, enterToProps, leaveToProps, reverse }?: FadeInHeightExpandTransitionOption): CNode[];
export {};
