import { CNode } from 'css-render';
interface IconSwitchTransitionOptions {
    originalTransform?: string;
    left?: string | number;
    top?: string | number;
    transition?: string;
}
export default function ({ originalTransform, left, top, transition }?: IconSwitchTransitionOptions): CNode[];
export {};
