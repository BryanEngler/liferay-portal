import {addParameters, configure} from '@storybook/react';

addParameters(
	{
		options: {

			/**
			 * Name to display in the top left corner
			 * Default: 'Storybook'
			 * @type {String}
			 */
			'theme.brandTitle': 'Search Ranking',

			/**
			 * Display panel that shows addon configurations.
			 * Default: true
			 * @type {Boolean}
			 */
			showPanel: false,

			/**
			 * Show/hide tool bar.
			 * Default: true
			 * @type {Boolean}
			 */
			isToolshown: false
		}
	}
);

function loadStories() {
	// eslint-disable-next-line global-require
	require('../stories');
}

configure(loadStories, module);