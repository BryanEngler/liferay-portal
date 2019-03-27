import React, {Component} from 'react';
import PropTypes from 'prop-types';
import ClayButton from './ClayButton.es';

class Tag extends Component {
	static propTypes = {
		label: PropTypes.string,
		onClickDelete: PropTypes.func
	};

	state = {
		label: this.props.label
	};

	_handleDelete = event => {
		event.preventDefault();

		this.props.onClickDelete(this.props.label);
	};

	render() {
		const {label} = this.props;

		return (
			<span className="label label-dismissible label-lg label-secondary">
				<span className="label-item label-item-expand">{label}</span>
				<span className="label-item label-item-after">
					<ClayButton
						aria-label="Close"
						className="close"
						iconName="times"
						onClick={this._handleDelete}
					/>
				</span>
			</span>
		);
	}
}

export default Tag;
