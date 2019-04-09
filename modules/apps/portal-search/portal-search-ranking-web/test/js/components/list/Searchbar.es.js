import React from 'react';
import SearchBar from 'components/list/SearchBar.es';
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
	'SearchBar',
	() => {
		afterEach(cleanup);

		it(
			'should have the searchbar term in the input',
			() => {
				const {getByPlaceholderText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[]}
					/>
				);

				const input = getByPlaceholderText('Contains Text');

				expect(input.value).toEqual('test');
			}
		);

		it(
			'should have an add result button when onAddResultSubmit is defined',
			() => {
				const onUpdateSearchBarTerm = jest.fn();

				const {queryByText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={onUpdateSearchBarTerm}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[]}
					/>
				);

				expect(queryByText('Add a Result')).not.toBeNull();
			}
		);

		it(
			'should not have an add result button when onAddResultSubmit is not defined',
			() => {
				const onUpdateSearchBarTerm = jest.fn();

				const {queryByText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={onUpdateSearchBarTerm}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[]}
					/>
				);

				expect(queryByText('Add a Result')).toBeNull();
			}
		);

		it(
			'should show what is selected using selectedIds',
			() => {

				const {queryByText, queryByPlaceholderText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[102, 103]}
					/>
				);

				expect(queryByPlaceholderText('Contains Text')).toBeNull();
				expect(queryByText('2 of 3 Items Selected')).not.toBeNull();
				expect(queryByText('Add a Result')).toBeNull();
			}
		);

		it(
			'should show the dropdown when clicked on',
			() => {

				const {container} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[102, 103]}
					/>
				);

				fireEvent.click(container.querySelector('button#optionDropdown'));

				expect(container.querySelector('.dropdown-menu')).toHaveClass('show');
			}
		);

		it(
			'should show no items selected with empty selectedIds',
			() => {
				const {queryByPlaceholderText, queryByText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[]}
					/>
				);

				expect(queryByText('Items Selected')).toBeNull();
				expect(queryByPlaceholderText('Contains Text')).not.toBeNull();
				expect(queryByText('Add a Result')).not.toBeNull();
			}
		);

		it(
			'should update the input value upon change',
			() => {
				const onUpdateSearchBarTerm = jest.fn();

				const {queryByPlaceholderText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={jest.fn()}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={onUpdateSearchBarTerm}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[]}
					/>
				);

				const input = queryByPlaceholderText('Contains Text');

				fireEvent.change(input, {target: {value: 'test'}});

				expect(input.value).toEqual('test');
			}
		);

		it(
			'should call the onLoadResults function when the searchbar enter is pressed',
			() => {
				const onSearchBarEnter = jest.fn();

				const {queryByPlaceholderText} = render(
					<SearchBar
						dataMap={DATA_MAP}
						onAddResultSubmit={jest.fn()}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onSearchBarEnter={onSearchBarEnter}
						onSelectAll={jest.fn()}
						onSelectClear={jest.fn()}
						onUpdateSearchBarTerm={jest.fn()}
						resultIds={[102, 104, 103]}
						searchBarTerm={'test'}
						selectedIds={[]}
					/>
				);

				const input = queryByPlaceholderText('Contains Text');

				fireEvent.change(input, {target: {value: 'test'}});

				fireEvent.keyDown(input, {key: 'Enter',
					keyCode: 13,
					which: 13
				});

				expect(onSearchBarEnter).toHaveBeenCalledTimes(1);
			}
		);
	}
);