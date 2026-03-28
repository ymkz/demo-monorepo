import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { spiceflowPlugin } from 'spiceflow/vite'
import { defineConfig } from 'vite'

export default defineConfig({
  clearScreen: false,
  plugins: [
    spiceflowPlugin({
      entry: './app/main.tsx',
    }),
    react(),
    tailwindcss(),
  ],
})
