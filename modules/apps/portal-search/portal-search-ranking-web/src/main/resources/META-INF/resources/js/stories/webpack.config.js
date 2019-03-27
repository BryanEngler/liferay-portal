const path = require('path');

/**
 * This is a custom webpack config for storybook builds.
 *
 * Exports a function which puts Storybook into full-control-mode. Accepts the
 * base config as the only param. `mode` has a value of 'DEVELOPMENT' or
 * 'PRODUCTION'. 'PRODUCTION' is used when building the static version of
 * storybook.
 *
 * Additional documentation can be read here:
 * https://storybook.js.org/docs/configurations/custom-webpack-config/
 */
module.exports = async ({ config, mode }) => {
	config.module.rules.push({
		test: /\.scss$/,
		loaders: ['style-loader', 'css-loader', 'sass-loader'],
		include: path.resolve(__dirname, '../../'),
	});

	return config;
};