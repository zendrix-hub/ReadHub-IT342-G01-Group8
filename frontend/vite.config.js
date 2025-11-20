import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import compression from 'vite-plugin-compression';

export default defineConfig({
  plugins: [
    react(),
    compression({
      algorithm: 'brotliCompress',
      ext: '.br',
      threshold: 512,
    }),
    compression({
      algorithm: 'gzip',
      ext: '.gz',
      threshold: 512,
    }),
  ],
  build: {
    target: 'es2018',
    assetsInlineLimit: 4096,
    chunkSizeWarningLimit: 600,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          network: ['axios'],
        },
      },
    },
  },
  css: {
    devSourcemap: false,
  },
});
