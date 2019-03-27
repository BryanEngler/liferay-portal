import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import ClayIcon from '../ClayIcon.es';
import Pagination from './Pagination.es';
import getCN from 'classnames';
import {getLang, sub} from 'utils/language.es';

const deltaValues = [5, 10, 20, 30, 50];

class DeltaItem extends React.Component {
	static propTypes = {
		delta: PropTypes.number.isRequired,
		href: PropTypes.string,
		onChange: PropTypes.func
	};

	_handleChange = () => {
		const {delta, onChange} = this.props;

		if (onChange) {
			onChange(delta);
		}
	};

	render() {
		const {href} = this.props;

		return (
			<a
				className="dropdown-item"
				href={href}
				onClick={this._handleChange}
			>
				{this.props.delta}
			</a>
		);
	}
}

class PaginationBar extends Component {
	static defaultProps = {
		deltas: deltaValues,
		selectedDelta: deltaValues[2],
		showDeltaDropdown: false
	};

	static propTypes = {
		deltas: PropTypes.array,
		href: PropTypes.string,
		onDeltaChange: PropTypes.func,
		onPageChange: PropTypes.func,
		page: PropTypes.number,
		selectedDelta: PropTypes.number,
		totalItems: PropTypes.number
	};

	constructor(props) {
		super(props);

		this.setWrapperRef = this.setWrapperRef.bind(this);
	}

	state = {
		showDeltaDropdown: false
	};

	componentDidMount() {
		document.addEventListener('mousedown', this._handleClickOutside);
	}

	componentWillUnmount() {
		document.removeEventListener('mousedown', this._handleClickOutside);
	}

	setWrapperRef(node) {
		this.wrapperRef = node;
	}

	_handleClickOutside = event => {
		if (this.wrapperRef && !this.wrapperRef.contains(event.target)) {
			this.setState({
				showDeltaDropdown: false
			});
		}
	};

	_handleDeltaChange = item => {
		this.props.onDeltaChange(item);

		this.setState({
			showDeltaDropdown: false
		});
	};

	_handleDropdownToggle = event => {
		event.preventDefault();

		this.setState(state => ({
			showDeltaDropdown: !state.showDeltaDropdown
		}));
	};

	render() {
		const {
			deltas,
			href,
			onPageChange,
			page,
			selectedDelta,
			totalItems
		} = this.props;

		const {showDeltaDropdown} = this.state;

		const classDeltaDropdown = getCN('dropdown-menu', 'dropdown-menu-top', {
			show: showDeltaDropdown
		});

		const start = page * selectedDelta;

		return (
			<div className="pagination-bar">
				<div
					className="dropdown pagination-items-per-page"
					ref={this.setWrapperRef}
				>
					<a
						aria-expanded="false"
						aria-haspopup="true"
						className="dropdown-toggle"
						data-toggle="dropdown"
						href={href}
						onClick={this._handleDropdownToggle}
					>
						{sub(getLang('x-items'), [selectedDelta])}
						<ClayIcon iconName="caret-double-l" />
					</a>
					<div className={classDeltaDropdown}>
						{deltas.map(item => (
							<DeltaItem
								delta={item}
								key={item}
								onChange={this._handleDeltaChange}
							/>
						))}
					</div>
				</div>
				<div className="pagination-results">
					{sub(getLang('showing-x-to-x-of-x-entries'), [
						start - selectedDelta + 1,
						Math.min(start, totalItems),
						totalItems
					])}
				</div>

				<Pagination
					href={href}
					onChange={onPageChange}
					page={page}
					total={Math.ceil(totalItems / selectedDelta)}
				/>
			</div>
		);
	}
}

export default PaginationBar;
