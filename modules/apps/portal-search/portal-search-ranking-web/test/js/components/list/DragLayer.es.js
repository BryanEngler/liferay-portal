import DragLayer from 'components/list/DragLayer.es';
import React from 'react';
import {cleanup, render} from 'react-testing-library';
import 'jest-dom/extend-expect';

describe(
	'DragLayer',
	() => {
		afterEach(cleanup);

		it(
			'should render when dragging',
			() => {
				const {container} = render(
					<DragLayer.DecoratedComponent dragging />
				);

				expect(container.firstChild).not.toBeNull();
				expect(container.firstChild).toBeVisible();
			}
		);

		it(
			'should not render by default',
			() => {
				const {container} = render(
					<DragLayer.DecoratedComponent />
				);

				expect(container.firstChild).toBeNull();
			}
		);
	}
);