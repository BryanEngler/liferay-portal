import React from 'react';
import PageToolbar from 'components/PageToolbar.es';
import {cleanup, render} from 'react-testing-library';
import 'jest-dom/extend-expect';

describe(
	'PageToolbar',
	() => {
		afterEach(cleanup);

		it(
			'should disable the publish button',
			() => {

				const {getByText} = render(
					<PageToolbar
						onCancel={'cancel'}
						submitDisabled={true}
					/>
				);

				expect(getByText('Publish')).toHaveAttribute('disabled');
			}
		);

		it(
			'should enable the publish button',
			() => {

				const {getByText} = render(
					<PageToolbar
						onCancel={'cancel'}
						submitDisabled={false}
					/>
				);

				expect(getByText('Publish')).not.toHaveAttribute('disabled');
			}
		);
	}
);