import { ComputedRef, Ref } from 'vue';
import type { HLJSApi } from 'highlight.js';
interface UseHljsProps {
    hljs?: unknown;
    [key: string]: unknown;
}
export interface Hljs {
    highlight: HLJSApi['highlight'];
    getLanguage: HLJSApi['getLanguage'];
}
export default function useHljs(props: UseHljsProps, shouldHighlightRef?: Ref<boolean>): ComputedRef<Hljs | undefined>;
export {};
