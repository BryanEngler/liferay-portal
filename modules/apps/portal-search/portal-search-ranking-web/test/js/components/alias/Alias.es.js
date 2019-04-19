import React from 'react';
import Alias from 'components/alias/index.es';
import {cleanup, fireEvent, render} from 'react-testing-library';
import 'jest-dom/extend-expect';

const MODAL_ID = 'alias-modal';

describe(
	'Alias',
	() => {
		afterEach(cleanup);

		it(
			'should have a list of tags available',
			() => {
				const {container} = render(
					<Alias
						keywords={['one', 'two', 'three']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				const tagsElement = container.querySelectorAll('.label-item-expand');

				expect(tagsElement[0]).toHaveTextContent('one');
				expect(tagsElement[1]).toHaveTextContent('two');
				expect(tagsElement[2]).toHaveTextContent('three');
			}
		);

		it(
			'should not show a modal by default',
			() => {
				const {queryByTestId} = render(
					<Alias
						keywords={['one', 'two', 'three']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				expect(queryByTestId(MODAL_ID)).toBeNull();
			}
		);

		it(
			'should render a modal when the add an alias button gets clicked',
			() => {
				const {getByText, queryByTestId} = render(
					<Alias
						keywords={['one', 'two', 'three']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				fireEvent.click(getByText('Add an Alias'));

				expect(queryByTestId(MODAL_ID)).not.toBeNull();
			}
		);

		it(
			'should close the modal after the cancel button gets clicked',
			() => {
				const {getByText, queryByTestId} = render(
					<Alias
						keywords={['one', 'two', 'three']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				fireEvent.click(getByText('Add an Alias'));

				fireEvent.click(getByText('Cancel'));

				expect(queryByTestId(MODAL_ID)).toBeNull();
			}
		);

		it(
			'should prompt to input an alias',
			() => {
				const {getByText, queryByText} = render(
					<Alias
						keywords={['one', 'two', 'three']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				fireEvent.click(getByText('Add an Alias'));

				expect(queryByText('Type a comma or press enter to input an alias')).not.toBeNull();
			}
		);

		it(
			'should have the modal with a default disabled add button',
			() => {
				const {getByText, queryByTestId} = render(
					<Alias
						keywords={['one', 'two', 'three']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				fireEvent.click(getByText('Add an Alias'));

				const modal = queryByTestId(MODAL_ID);

				expect(modal.querySelector('.modal-footer .btn-primary')).toHaveAttribute('disabled');
			}
		);

		it(
			'should not render blank keywords',
			() => {
				const {container} = render(
					<Alias
						keywords={['', ' ']}
						onClickDelete={jest.fn()}
						onClickSubmit={jest.fn()}
						searchTerm={'example'}
					/>
				);

				const tagsElement = container.querySelectorAll('.label-item');

				expect(tagsElement.length).toBe(0);
			}
		);
	}
);