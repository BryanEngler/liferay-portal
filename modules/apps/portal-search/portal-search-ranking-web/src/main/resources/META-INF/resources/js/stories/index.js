import React from 'react';
import {addDecorator, storiesOf} from '@storybook/react';
import {array, boolean, text, withKnobs} from '@storybook/addon-knobs';
import {withA11y} from '@storybook/addon-a11y';

import '../../css/main.scss';

import Alias from 'components/alias/index.es';
import ClayEmptyState from 'components/shared/ClayEmptyState.es';
import List from 'components/list/index.es';
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

const withSheet = storyFn => (
	<div className="sheet sheet-lg" style={{marginTop: '24px'}}>
		{storyFn()}
	</div>
);

const noop = () => {};

storiesOf('Main|ResultsRankingForm', module).add(
	'default',
	() => <ResultsRankingForm cancelUrl="" searchTerm={text('Search Term', 'example')} />
);

storiesOf('Components|PageToolbar', module)
	.add('default', () => <PageToolbar submitDisabled={boolean('Disabled', false)} />);

storiesOf('Components|Alias', module)
	.addDecorator(withSheet)
	.add('default', () => <Alias keywords={array('Keywords', [], ',')} />);

storiesOf('Components|List', module)
	.addDecorator(withSheet)
	.add(
		'default',
		() => (
			<List
				dataLoading={false}
				dataMap={{}}
				onAddResultSubmit={noop}
			/>
		)
	);

storiesOf('Components|EmptyState', module)
	.addDecorator(withSheet)
	.add(
		'default',
		() => (
			<ClayEmptyState
				description={text('Description')}
				title={text('Title')}
			/>
		)
	);