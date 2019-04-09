import React from 'react';
import Tag from 'components/alias/Tag.es';
import {cleanup, fireEvent, render} from 'react-testing-library';
import 'jest-dom/extend-expect';

describe(
	'Tag',
	() => {
		afterEach(cleanup);

		it(
			'should have corresponding label',
			() => {

				const {container} = render(
					<Tag
						label="one"
						onClickDelete={jest.fn()}
					/>
				);

				const tag = container.querySelector('.label-item-expand');

				expect(tag).toHaveTextContent('one');
			}
		);

		it(
			'should call the onClickDelete function when it gets clicked on',
			() => {
				const onClickDelete = jest.fn();

				const {container} = render(
					<Tag
						label="one"
						onClickDelete={onClickDelete}
					/>
				);

				fireEvent.click(container.querySelector('button.close'));

				expect(onClickDelete.mock.calls.length).toBe(1);
			}
		);
	}
);