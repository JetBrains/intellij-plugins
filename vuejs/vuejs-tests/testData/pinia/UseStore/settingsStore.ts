import {defineStore} from "pinia";
import {useLocalStorage} from "@vueuse/core";

export const useSettingsStore = defineStore(
    'settings', {
        state: () => ({
            isAutoChangeTheme: useLocalStorage('isAutoChangeTheme', true),
            isDarkMode: useLocalStorage('isDarkMode', false),
            isOfflineMode: useLocalStorage('isOfflineMode', false)
        }),
    });