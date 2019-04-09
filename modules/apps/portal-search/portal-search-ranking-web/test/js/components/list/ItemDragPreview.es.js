import ItemDragPreview from 'components/list/ItemDragPreview.es';
import React from 'react';
import {cleanup, render} from 'react-testing-library';
import {mockDocument} from 'test/mock-data';
import 'jest-dom/extend-expect';

// Mock pinned document since only pinned results can be dragged.

const MOCK_DOCUMENT = mockDocument(1, {pinned: true});

/**
 * Tests a text string if the value is displayed in the component.
 * @param {string} text The text to test.
 */
function testText(text) {
	const {getByText} = render(
		<ItemDragPreview {...MOCK_DOCUMENT} />
	);

	expect(getByText(text, {exact: false})).toBeInTheDocument();
}

describe(
	'ItemDragPreview',
	() => {
		afterEach(cleanup);

		it(
			'should display the title',
			() => {
				testText(MOCK_DOCUMENT.title);
			}
		);

		it(
			'should display the description',
			() => {
				testText(MOCK_DOCUMENT.description);
			}
		);

		it(
			'should display the author',
			() => {
				testText(MOCK_DOCUMENT.author);
			}
		);

		it(
			'should display the clicks',
			() => {
				testText(`${MOCK_DOCUMENT.clicks}`);
			}
		);

		it(
			'should display the date',
			() => {
				testText(`${MOCK_DOCUMENT.date}`);
			}
		);

		it(
			'should display the drag handle',
			() => {
				const {getByTestId} = render(
					<ItemDragPreview {...MOCK_DOCUMENT} />
				);

				expect(getByTestId('DRAG_ICON')).toBeVisible();
			}
		);
	}
);