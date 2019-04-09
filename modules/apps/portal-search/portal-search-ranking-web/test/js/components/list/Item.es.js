import React from 'react';
import Item from 'components/list/Item.es';
import {cleanup, fireEvent, render} from 'react-testing-library';
import 'jest-dom/extend-expect';

jest.mock('react-dnd', () => ({
	DragSource: el => el => el,
	DropTarget: el => el => el
}));

describe(
	'Item',
	() => {
		afterEach(cleanup);

		it(
			'should show the dropdown when clicked on',
			() => {

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				fireEvent.click(container.querySelector('button#optionDropdown'));

				expect(container.querySelector('.dropdown-menu')).toHaveClass('show');
			}
		);

		it(
			'should show the appropriate subtext',
			() => {

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				const subtitles = container.querySelectorAll('.list-group-subtext');

				expect(subtitles[0]).toHaveTextContent('Test Test - Apr 18 2018, 11:04 AM');
				expect(subtitles[1]).toHaveTextContent('[Web Content]');
			}
		);

		it(
			'should show the appropriate title',
			() => {

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				expect(container.querySelector('.text-truncate-inline')).toHaveTextContent('This is a Web Content Example');
			}
		);

		it(
			'should show the appropriate description',
			() => {

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				expect(container.querySelector('.list-item-description')).toHaveTextContent('Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ips...');
			}
		);

		it(
			'should show the appropriate view count',
			() => {

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={jest.fn()}
						onClickPin={jest.fn()}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				expect(container.querySelector('.click-count')).toHaveTextContent('289');
			}
		);

		it(
			'should call the onClickHide function when its button gets clicked on',
			() => {

				const onClickHide = jest.fn();

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={onClickHide}
						onClickPin={jest.fn()}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				fireEvent.click(container.querySelector('.result-hide button'));

				expect(onClickHide.mock.calls.length).toBe(1);
			}
		);

		it(
			'should call the onClickPin function when its button gets clicked on',
			() => {

				const onClickPin = jest.fn();

				const {container} = render(
					<Item
						addedResult={false}
						author={'Test Test'}
						clicks={289}
						date={'Apr 18 2018, 11:04 AM'}
						description={'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod'}
						extension={''}
						hidden={false}
						hoverIndex={jest.fn()}
						id={101}
						index={1}
						key={101}
						lastIndex={3}
						onClickHide={jest.fn()}
						onClickPin={onClickPin}
						onDragHover={jest.fn()}
						onMove={jest.fn()}
						onRemoveSelect={jest.fn()}
						onSelect={jest.fn()}
						pinned={true}
						selected={true}
						title={'This is a Web Content Example'}
						type={'Web Content'}
					/>
				);

				fireEvent.click(container.querySelector('.result-pin button'));

				expect(onClickPin.mock.calls.length).toBe(1);
			}
		);
	}
);