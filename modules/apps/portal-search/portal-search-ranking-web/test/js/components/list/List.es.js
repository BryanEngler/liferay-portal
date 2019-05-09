import React from 'react';
import List from 'components/list/index.es';
import {cleanup, fireEvent, render} from 'react-testing-library';
import 'jest-dom/extend-expect';
import {getMockResultsData} from 'test/mock-data.js';
import {resultsDataToMap} from 'utils/util.es';

const DATA_MAP = resultsDataToMap(
	getMockResultsData(
		10,
		0,
		'',
		false
	).documents
);

describe(
	'List',
	() => {
		afterEach(cleanup);

		it(
			'should list out results in order with expected titles',
			() => {

				const {container} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				const listItems = container.querySelectorAll('.text-truncate-inline');

				expect(listItems[0]).toHaveTextContent('102 This is a Document Example');
				expect(listItems[1]).toHaveTextContent('104 This is a Document Example');
				expect(listItems[2]).toHaveTextContent('103 This is a Web Content Example');
			}
		);

		it(
			'should have no loading icon',
			() => {

				const {container} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				expect(container.querySelector('.loading-animation')).toBeNull();
				expect(container.querySelector('.load-more-button')).not.toBeNull();
			}
		);

		it(
			'should have a loading icon',
			() => {
				const {container} = render(
					<List
						dataLoading={true}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				expect(container.querySelector('.loading-animation')).not.toBeNull();
				expect(container.querySelector('.load-more-button')).toBeNull();
			}
		);

		it(
			'should call the onLoadResults function when the loading button is clicked',
			() => {
				const mockLoad = jest.fn();

				const {container} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={mockLoad}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				const loadButton = container.querySelector('.load-more-button');

				fireEvent.click(loadButton);

				expect(mockLoad).toHaveBeenCalledTimes(1);
			}
		);

		it(
			'should update the selected ids',
			() => {
				const mockLoad = jest.fn();

				const {getByTestId, queryByText} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={mockLoad}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				fireEvent.click(getByTestId('102').querySelector('.custom-control-input'));
				fireEvent.click(getByTestId('104').querySelector('.custom-control-input'));

				expect(queryByText('2 of 3 Items Selected')).toBeInTheDocument();
			}
		);

		it(
			'should update the selected ids back',
			() => {
				const mockLoad = jest.fn();

				const {getByTestId, queryByText} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={mockLoad}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				fireEvent.click(getByTestId('102').querySelector('.custom-control-input'));
				fireEvent.click(getByTestId('102').querySelector('.custom-control-input'));

				expect(queryByText('Items Selected')).not.toBeInTheDocument();
			}
		);

		it(
			'should focus on the id',
			() => {
				const mockLoad = jest.fn();

				const {getByTestId} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={mockLoad}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				fireEvent.focus(getByTestId('102'));

				const focusedElement = document.activeElement;

				expect(focusedElement).toBe(getByTestId('102'));
			}
		);

		it(
			'should add classes of focus and reorder on the id',
			() => {
				const mockLoad = jest.fn();

				const {getByTestId} = render(
					<List
						dataLoading={false}
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onLoadResults={mockLoad}
						onSearchBarEnter={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={''}
						selected={[104]}
						totalResultsCount={300}
					/>
				);

				fireEvent.focus(getByTestId('102'));

				fireEvent.keyDown(getByTestId('102'), {key: ' ',
					code: 32
				});

				expect(getByTestId('102')).toHaveClass('results-ranking-item-focus');
				expect(getByTestId('102')).not.toHaveClass('results-ranking-item-reorder');
			}
		);
	}
);