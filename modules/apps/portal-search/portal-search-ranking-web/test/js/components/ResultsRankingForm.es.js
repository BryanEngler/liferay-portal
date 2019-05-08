import React from 'react';
import ResultsRankingForm from 'components/ResultsRankingForm.es';
import {cleanup, fireEvent, render, waitForElement, within} from 'react-testing-library';
import 'jest-dom/extend-expect';

jest.mock('utils/api.es');

const RESULTS_LIST_ID = 'results-list-group';

const HIDDEN_IDS_ADDED_INPUT_SELECTOR = '#hiddenIdsAdded';

const HIDDEN_IDS_REMOVED_INPUT_SELECTOR = '#hiddenIdsRemoved';

const HIDE_BUTTON_LABEL = 'Hide Result';

const SHOW_BUTTON_LABEL = 'Show Result';

describe(
	'ResultsRankingForm',
	() => {
		afterEach(cleanup);

		it(
			'should render the results ranking form',
			() => {

				const {container} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={'example'}
					/>
				);

				expect(container.querySelector('.results-ranking-form-root')).toBeInTheDocument();
			}
		);

		it(
			'should render the results ranking form after loading',
			async() => {

				const {getByTestId} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				expect(getByTestId(RESULTS_LIST_ID)).toBeInTheDocument();
			}
		);

		it(
			'should render the results ranking form after loading hidden tab',
			async() => {

				const {getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				fireEvent.click(getByText('Hidden'));

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				expect(getByTestId(RESULTS_LIST_ID)).toBeInTheDocument();
			}
		);

		it(
			'should include the initial aliases',
			async() => {

				const {container} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						initialAliases={['one', 'two', 'three']}
						searchTerm={''}
					/>
				);

				const tagsElement = container.querySelectorAll('.label-item-expand');

				expect(tagsElement[0]).toHaveTextContent('one');
				expect(tagsElement[1]).toHaveTextContent('two');
				expect(tagsElement[2]).toHaveTextContent('three');
			}
		);

		it(
			'should remove an initial alias after clicking delete',
			async() => {

				const {container} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						initialAliases={['one', 'two', 'three']}
						searchTerm={''}
					/>
				);

				const tagsElementClose = container.querySelectorAll('.label-item-after button');

				fireEvent.click(tagsElementClose[0]);

				const tagsElement = container.querySelectorAll('.label-item-expand');

				expect(tagsElement[0]).not.toHaveTextContent('one');
			}
		);

		xit(
			'should update the pinnedAdded',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(getByTestId('109').querySelector('.result-pin button'));

				expect(container.querySelector('#pinnedAdded').value).toEqual('109');

				expect(getByText('Publish')).not.toHaveAttribute('disabled');
			}
		);

		xit(
			'should update the pinnedAdded back',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(getByTestId('109').querySelector('.result-pin button'));

				fireEvent.click(getByTestId('109').querySelector('.result-pin button'));

				expect(container.querySelector('#pinnedAdded').value).toEqual('');

				expect(getByText('Publish')).toHaveAttribute('disabled');
			}
		);

		xit(
			'should update the pinnedRemoved',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(getByTestId('100').querySelector('.result-pin button'));

				expect(container.querySelector('#pinnedRemoved').value).toEqual('100');

				expect(getByText('Publish')).not.toHaveAttribute('disabled');
			}
		);

		xit(
			'should update the pinnedRemoved back',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(getByTestId('100').querySelector('.result-pin button'));

				fireEvent.click(getByTestId('100').querySelector('.result-pin button'));

				expect(container.querySelector('#pinnedRemoved').value).toEqual('');

				expect(getByText('Publish')).toHaveAttribute('disabled');
			}
		);

		it(
			'should update the hiddenAdded',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(within(getByTestId('100')).getByTitle(HIDE_BUTTON_LABEL));

				expect(container.querySelector(HIDDEN_IDS_ADDED_INPUT_SELECTOR).value).toEqual('100');

				expect(getByText('Publish')).not.toHaveAttribute('disabled');
			}
		);

		it(
			'should update the hiddenAdded back',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(within(getByTestId('105')).getByTitle(HIDE_BUTTON_LABEL));

				fireEvent.click(getByText('Hidden'));

				fireEvent.click(within(getByTestId('105')).getByTitle(SHOW_BUTTON_LABEL));

				expect(container.querySelector(HIDDEN_IDS_ADDED_INPUT_SELECTOR).value).toEqual('');

				expect(getByText('Publish')).toHaveAttribute('disabled');
			}
		);

		it(
			'should update the hiddenRemoved',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				fireEvent.click(getByText('Hidden'));

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(within(getByTestId('200')).getByTitle(SHOW_BUTTON_LABEL));

				expect(container.querySelector(HIDDEN_IDS_REMOVED_INPUT_SELECTOR).value).toEqual('200');

				expect(getByText('Publish')).not.toHaveAttribute('disabled');
			}
		);

		it(
			'should update the hiddenRemoved back',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				fireEvent.click(getByText('Hidden'));

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(within(getByTestId('200')).getByTitle(SHOW_BUTTON_LABEL));

				fireEvent.click(getByText('Visible'));

				fireEvent.click(within(getByTestId('200')).getByTitle(HIDE_BUTTON_LABEL));

				expect(container.querySelector(HIDDEN_IDS_REMOVED_INPUT_SELECTOR).value).toEqual('');

				expect(getByText('Publish')).toHaveAttribute('disabled');
			}
		);

		xit(
			'should update the pinnedRemoved from hiding a result',
			async() => {

				const {container, getByTestId, getByText} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(getByTestId('100').querySelector('.result-hide button'));

				expect(container.querySelector('#pinnedRemoved').value).toEqual('100');

				expect(container.querySelector(HIDDEN_IDS_ADDED_INPUT_SELECTOR).value).toEqual('100');

				fireEvent.click(getByText('Hidden'));

				fireEvent.click(getByTestId('100').querySelector('.result-hide button'));

				expect(container.querySelector('#pinnedRemoved').value).toEqual('100');

				expect(container.querySelector(HIDDEN_IDS_ADDED_INPUT_SELECTOR).value).toEqual('');

				expect(getByText('Publish')).not.toHaveAttribute('disabled');
			}
		);

		it(
			'should fetch more results after clicking on load more button',
			async() => {

				const {container, getByTestId} = render(
					<ResultsRankingForm
						cancelUrl={'cancel'}
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm={''}
					/>
				);

				await waitForElement(() => getByTestId(RESULTS_LIST_ID));

				fireEvent.click(container.querySelector('.load-more-button'));

				await waitForElement(() => getByTestId('110'));

				expect(getByTestId(RESULTS_LIST_ID)).toHaveTextContent('110 This is a Document Example');
				expect(getByTestId(RESULTS_LIST_ID)).toHaveTextContent('119 This is a Web Content Example');
			}
		);

		it(
			'should have the same pinned end index if there are no additional pinned items loaded',
			async() => {

				const {container, getByTestId} = render(
					<ResultsRankingForm
						cancelUrl=""
						fetchDocumentsHiddenUrl=""
						fetchDocumentsUrl=""
						searchTerm=""
					/>
				);

				const pinnedIdsEndIndexInput = container.querySelector('#pinnedIdsEndIndex');

				await waitForElement(() => getByTestId('100'));

				expect(pinnedIdsEndIndexInput.value).toBe('4');

				fireEvent.click(container.querySelector('.load-more-button'));

				await waitForElement(() => getByTestId('110'));

				expect(pinnedIdsEndIndexInput.value).toBe('4');
			}
		);
	}
);