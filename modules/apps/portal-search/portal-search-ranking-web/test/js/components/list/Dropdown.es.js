import React from 'react';
import Dropdown from 'components/list/Dropdown.es';
import {cleanup, fireEvent, render} from 'react-testing-library';
import 'jest-dom/extend-expect';

describe(
	'Dropdown',
	() => {
		afterEach(cleanup);

		it(
			'should have option to unpin visible',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={true}
					/>
				);

				expect(queryByText('Unpin Result')).not.toBeNull();
				expect(queryByText('Unpin Results')).toBeNull();
			}
		);

		it(
			'should have option to unpin multiple visible',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						itemCount={2}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={true}
					/>
				);

				expect(queryByText('Unpin Results')).not.toBeNull();
			}
		);

		it(
			'should have option to pin visible',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Pin Result')).not.toBeNull();
				expect(queryByText('Pin Results')).toBeNull();
			}
		);

		it(
			'should have option to unpin multiple visible',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						itemCount={2}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Pin Results')).not.toBeNull();
			}
		);

		it(
			'should have option to hide visible',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Hide Result')).not.toBeNull();
				expect(queryByText('Hide Results')).toBeNull();
			}
		);

		it(
			'should have option to hide multiple visible',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						itemCount={2}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Hide Results')).not.toBeNull();
			}
		);

		it(
			'should have option to show hidden',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={true}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Show Result')).not.toBeNull();
				expect(queryByText('Show Results')).toBeNull();
			}
		);

		it(
			'should have option to show multiple hidden',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={false}
						hidden={true}
						itemCount={2}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Show Results')).not.toBeNull();
			}
		);

		it(
			'should not have option to show for added results',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={true}
						hidden={false}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={true}
					/>
				);

				expect(queryByText('Show Result')).toBeNull();
			}
		);

		it(
			'should have option to pin hidden',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={true}
						hidden={true}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Pin Result')).not.toBeNull();
				expect(queryByText('Pin Results')).toBeNull();
			}
		);

		it(
			'should have option to pin multiple hidden',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={true}
						hidden={true}
						itemCount={2}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				expect(queryByText('Pin Results')).not.toBeNull();
			}
		);

		it(
			'should have not have option to show/hide when onClickHide is missing',
			() => {

				const {queryByText} = render(
					<Dropdown
						addedResult={true}
						hidden={false}
						onClickPin={jest.fn()}
						pinned={true}
					/>
				);

				expect(queryByText('Show Result')).toBeNull();
				expect(queryByText('Hide Result')).toBeNull();
			}
		);

		it(
			'should show the dropdown when clicked on',
			() => {

				const {container} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				fireEvent.click(container.querySelector('button#optionDropdown'));

				expect(container.querySelector('.dropdown-menu')).toHaveClass('show');
			}
		);

		it(
			'should call the onClickHide function when it gets clicked on',
			() => {

				const onClickHide = jest.fn();

				const {getByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						onClickHide={onClickHide}
						onClickPin={jest.fn()}
						pinned={false}
					/>
				);

				fireEvent.click(getByText('Hide Result'));

				expect(onClickHide.mock.calls.length).toBe(1);
			}
		);

		it(
			'should call the onClickPin function when it gets clicked on',
			() => {

				const onClickPin = jest.fn();

				const {getByText} = render(
					<Dropdown
						addedResult={false}
						hidden={false}
						onClickHide={jest.fn()}
						onClickPin={onClickPin}
						pinned={false}
					/>
				);

				fireEvent.click(getByText('Pin Result'));

				expect(onClickPin.mock.calls.length).toBe(1);
			}
		);
	}
);