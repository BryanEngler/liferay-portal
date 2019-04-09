import ClayMultiselect from 'components/shared/ClayMultiselect.es';
import React from 'react';
import {cleanup, render} from 'react-testing-library';

describe(
	'ClayMultiselect',
	() => {
		afterEach(cleanup);

		it(
			'should render',
			() => {
				const {container} = render(
					<ClayMultiselect />
				);

				expect(container.firstChild).not.toBeNull();
			}
		);
	}
);