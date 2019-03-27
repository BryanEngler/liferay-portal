const proxy = require('http-proxy-middleware')

/**
 * Middleware to proxy portal resources. This was first created to be able to
 * reference icons in the admin theme from Storybook.
 */
module.exports = function expressMiddleware(router) {
	router.use('/o', proxy({
		target: 'http://0.0.0.0:8080',
		changeOrigin: true
	}))
};