import { writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const hybridUrl = (process.env.DLR_HYBRID_API_BASE_URL ?? '').trim();
const configuredUrl = (hybridUrl || process.env.DLR_API_BASE_URL || 'http://localhost:8081').trim().replace(/\/+$/, '');
const apiBaseUrl = configuredUrl.endsWith('/api') ? configuredUrl : `${configuredUrl}/api`;
const executionAvailable = (process.env.DLR_EXECUTION_AVAILABLE ?? 'true').toLowerCase() === 'true';
const environment = process.env.DLR_DEPLOYMENT_ENVIRONMENT ?? 'local';
const output = fileURLToPath(new URL('../src/runtime-config.js', import.meta.url));
const config = { apiBaseUrl, executionAvailable, environment };

writeFileSync(output, `window.__DLR_CONFIG__ = ${JSON.stringify(config)};\n`, 'utf8');
console.log(`Configuration DLR générée pour ${environment}: ${apiBaseUrl}`);

