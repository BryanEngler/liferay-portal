import {configure} from '@storybook/react';

function loadStories() {
	// eslint-disable-next-line global-require
	require('../stories');
}

configure(loadStories, module);