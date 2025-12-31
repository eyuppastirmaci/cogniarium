import { expect, afterEach } from 'vitest';
import { cleanup } from '@vue/test-utils';
import '@testing-library/jest-dom/vitest';

// Cleanup after each test
afterEach(() => {
  cleanup();
  // Clear localStorage
  localStorage.clear();
});

