import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DraftStoreService {
  private readonly databaseName = 'dlr-local';
  private readonly storeName = 'drafts';

  async load(labCode: string): Promise<string | null> {
    const database = await this.open();
    if (!database) return localStorage.getItem(`dlr:draft:${labCode}`);
    return new Promise((resolve) => {
      const request = database.transaction(this.storeName, 'readonly').objectStore(this.storeName).get(labCode);
      request.onsuccess = () => resolve(typeof request.result?.sourceCode === 'string' ? request.result.sourceCode : null);
      request.onerror = () => resolve(null);
    });
  }

  async save(labCode: string, sourceCode: string): Promise<void> {
    const database = await this.open();
    if (!database) {
      localStorage.setItem(`dlr:draft:${labCode}`, sourceCode);
      return;
    }
    await new Promise<void>((resolve) => {
      const transaction = database.transaction(this.storeName, 'readwrite');
      transaction.objectStore(this.storeName).put({ labCode, sourceCode, updatedAt: new Date().toISOString() });
      transaction.oncomplete = () => resolve();
      transaction.onerror = () => resolve();
    });
  }

  async remove(labCode: string): Promise<void> {
    localStorage.removeItem(`dlr:draft:${labCode}`);
    const database = await this.open();
    if (!database) return;
    database.transaction(this.storeName, 'readwrite').objectStore(this.storeName).delete(labCode);
  }

  private open(): Promise<IDBDatabase | null> {
    if (!('indexedDB' in globalThis)) return Promise.resolve(null);
    return new Promise((resolve) => {
      const request = indexedDB.open(this.databaseName, 1);
      request.onupgradeneeded = () => {
        if (!request.result.objectStoreNames.contains(this.storeName)) {
          request.result.createObjectStore(this.storeName, { keyPath: 'labCode' });
        }
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => resolve(null);
    });
  }
}
