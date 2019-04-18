import React from 'react';
import ResultsRankingForm from 'components/ResultsRankingForm.es';
import {cleanup, fireEvent, render, waitForElement} from 'react-testing-library';
import 'jest-dom/extend-expect';

jest.mock('utils/api.es');

const RESULTS_LIST_ID = 'results-list-group';

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

		it(
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

		it(
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

		it(
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

				fireEvent.click(getByTestId('100').querySelector('.result-hide button'));

				expect(container.querySelector('#hiddenAdded').value).toEqual('100');

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

				fireEvent.click(getByTestId('105').querySelector('.result-hide button'));

				fireEvent.click(getByText('Hidden'));

				fireEvent.click(getByTestId('105').querySelector('.result-hide button'));

				expect(container.querySelector('#hiddenAdded').value).toEqual('');

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

				fireEvent.click(getByTestId('200').querySelector('.result-hide button'));

				expect(container.querySelector('#hiddenRemoved').value).toEqual('200');

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

				fireEvent.click(getByTestId('200').querySelector('.result-hide button'));

				fireEvent.click(getByText('Visible'));

				fireEvent.click(getByTestId('200').querySelector('.result-hide button'));

				expect(container.querySelector('#hiddenRemoved').value).toEqual('');

				expect(getByText('Publish')).toHaveAttribute('disabled');
			}
		);

		it(
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

				expect(container.querySelector('#hiddenAdded').value).toEqual('100');

				fireEvent.click(getByText('Hidden'));

				fireEvent.click(getByTestId('100').querySelector('.result-hide button'));

				expect(container.querySelector('#pinnedRemoved').value).toEqual('100');

				expect(container.querySelector('#hiddenAdded').value).toEqual('');

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
	}
);