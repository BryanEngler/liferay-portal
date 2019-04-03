import React, {Component} from 'react';
import {PropTypes} from 'prop-types';

const DEFAULT_DESCRIPTION = Liferay.Language.get('sorry-there-are-no-results-found');

const DEFAULT_TITLE = Liferay.Language.get('no-results-found');

const DISPLAY_STATES = {
	EMPTY: 'empty',
	SEARCH: 'search',
	SUCCESS: 'success'
};

class ClayEmptyState extends Component {
	static propTypes = {
		description: PropTypes.string,
		displayState: PropTypes.oneOf(
			[
				DISPLAY_STATES.EMPTY,
				DISPLAY_STATES.SEARCH,
				DISPLAY_STATES.SUCCESS
			]
		),
		title: PropTypes.string
	};

	static defaultProps = {
		displayState: DISPLAY_STATES.SEARCH
	};

	render() {
		const {description, displayState, title} = this.props;

		return (
			<div className="empty-state-root">
				<img
					alt={Liferay.Language.get('empty-state-image')}
					className="empty-state-image"
					src={`/o/admin-theme/images/states/${displayState}_state.gif`}
				/>

				<div className="empty-state-title">
					{title || DEFAULT_TITLE}
				</div>

				<div className="empty-state-description">
					{description || DEFAULT_DESCRIPTION}
				</div>
			</div>
		);
	}
}

export default ClayEmptyState;