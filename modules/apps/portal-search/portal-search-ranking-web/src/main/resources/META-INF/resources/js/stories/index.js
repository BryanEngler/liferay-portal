import React from 'react';
import {addDecorator, storiesOf} from '@storybook/react';
import {array, boolean, text, withKnobs} from '@storybook/addon-knobs';
import {withA11y} from '@storybook/addon-a11y';

import '../../css/main.scss';

import Alias from 'components/alias/index.es';
import PageToolbar from 'components/PageToolbar.es';
import ResultsRankingForm from 'components/ResultsRankingForm.es';
import ThemeContext from 'ThemeContext.es';

addDecorator(withA11y);
addDecorator(withKnobs);

addDecorator(
	storyFn => {
		const context = {
			spritemap: '/o/admin-theme/images/lexicon/icons.svg'
		};

		return (
			<ThemeContext.Provider value={context}>
				<div className="results-rankings-root">
					{storyFn()}
				</div>
			</ThemeContext.Provider>
		);
	}
);
);

storiesOf('ResultsRankingForm', module).add(
	'default',
	() => <ResultsRankingForm cancelUrl="" searchTerm={text('Search Term', 'example')} />
);

storiesOf('PageToolbar', module)
	.add('default', () => <PageToolbar submitDisabled={boolean('Disabled', false)} />);

storiesOf('Alias', module)
	.add('default', () => <Alias keywords={array('Keywords', [], ',')} />);
