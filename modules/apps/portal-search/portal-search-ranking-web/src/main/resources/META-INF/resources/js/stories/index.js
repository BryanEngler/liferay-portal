import React from 'react';
import {action} from '@storybook/addon-actions';
import {addDecorator, storiesOf} from '@storybook/react';
import {array, boolean, select, text, withKnobs} from '@storybook/addon-knobs';
import {withA11y} from '@storybook/addon-a11y';

import Alias from 'components/alias/index.es';
import ClayEmptyState from 'components/shared/ClayEmptyState.es';
import List from 'components/list/index.es';
import PageToolbar from 'components/PageToolbar.es';
import ResultsRankingForm from 'components/ResultsRankingForm.es';
import ThemeContext from 'ThemeContext.es';
import {mockDataMap} from 'stories/mock-data';

import '../../css/main.scss';

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

storiesOf('Main|ResultsRankingForm', module)
	.add(
		'default',
		() => (
			<ResultsRankingForm
				cancelUrl=""
				fetchDocumentsHiddenUrl=""
				fetchDocumentsUrl=""
				searchTerm={text('Search Term', 'example')}
			/>
		)
	)
	.add(
		'with mock api',
		() => (
			<ResultsRankingForm
				cancelUrl=""
				fetchDocumentsHiddenUrl="http://www.mocky.io/v2/5cabd9ab3000002900103266"
				fetchDocumentsUrl="http://www.mocky.io/v2/5cabd1073000002900103260"
				searchTerm={text('Search Term', 'example')}
			/>
		)
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
				dataMap={mockDataMap}
				fetchDocumentsUrl=""
				onAddResultSubmit={action('onAddResultSubmit')}
				onClickHide={action('onClickHide')}
				onClickPin={action('onClickPin')}
				onMove={action('onMove')}
				resultIds={['1', '2', '3', '4', '5']}
			/>
		)
	)
	.add(
		'empty',
		() => (
			<List
				dataLoading={false}
				dataMap={{}}
				fetchDocumentsUrl=""
				onAddResultSubmit={action('onAddResultSubmit')}
			/>
		)
	)
	.add(
		'error',
		() => (
			<List
				dataLoading={false}
				dataMap={{}}
				displayError
				fetchDocumentsUrl=""
				onAddResultSubmit={action('onAddResultSubmit')}
				onLoadResults={action('load-results')}
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
				displayState={
					select(
						'Display State',
						{
							Empty: 'empty',
							Search: 'search',
							Success: 'success'
						},
						'search'
					)
				}
				title={text('Title')}
			/>
		)
	)
	.add(
		'with action',
		() => (
			<ClayEmptyState
				actionLabel="Refresh"
				description={text('Description')}
				displayState="empty"
				onClickAction={action('onClickAction')}
				title={text('Title')}
			/>
		)
	);