const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081, // Frontend dev server port
    proxy: {
      '/api': { // Match paths starting with /api
        target: 'http://localhost:8080', // Backend server
        changeOrigin: true,
        // pathRewrite: {'^/api' : ''} // Optional: if backend doesn't have /api prefix
      }
    }
  }
})
