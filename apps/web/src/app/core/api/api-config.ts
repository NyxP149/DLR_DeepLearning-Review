export interface DlrRuntimeConfig {
  apiBaseUrl?: string;
  executionAvailable?: boolean;
  environment?: string;
}

declare global {
  interface Window {
    __DLR_CONFIG__?: DlrRuntimeConfig;
  }
}

const runtimeConfig = typeof window === 'undefined' ? undefined : window.__DLR_CONFIG__;
const configuredBaseUrl = runtimeConfig?.apiBaseUrl?.trim() || 'http://localhost:8081/api';

export const API_BASE_URL = configuredBaseUrl.replace(/\/+$/, '');
export const API_ORIGIN = API_BASE_URL.replace(/\/api$/, '');
export const EXECUTION_AVAILABLE = runtimeConfig?.executionAvailable ?? true;
export const DEPLOYMENT_ENVIRONMENT = runtimeConfig?.environment ?? 'local';

