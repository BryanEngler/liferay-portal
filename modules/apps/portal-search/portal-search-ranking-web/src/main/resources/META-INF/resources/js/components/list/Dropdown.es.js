import ClayButton from 'components/shared/ClayButton.es';
import ClayIcon from 'components/shared/ClayIcon.es';
import getCN from 'classnames';
import React, {Component} from 'react';
import {getPluralMessage} from 'utils/language.es';
import {PropTypes} from 'prop-types';

class Dropdown extends Component {
	static propTypes = {
		addedResult: PropTypes.bool,
		hidden: PropTypes.bool,
		itemCount: PropTypes.number,
		onClickHide: PropTypes.func,
		onClickPin: PropTypes.func,
		pinned: PropTypes.bool
	};

	static defaultProps = {
		addedResult: false,
		itemCount: 1
	};

	constructor(props) {
		super(props);

		this.setWrapperRef = this.setWrapperRef.bind(this);
	}

	state = {
		show: false
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
			this.setState({show: false});
		}
	};

	_handleDropdownToggle = event => {
		event.preventDefault();

		this.setState(
			state => ({show: !state.show})
		);
	};

	_handleDropdownAction = actionFn => event => {
		event.preventDefault();

		actionFn(event);

		this.setState({show: false});
	};

	render() {
		const {
			addedResult,
			hidden,
			itemCount,
			onClickHide,
			onClickPin,
			pinned
		} = this.props;

		const {show} = this.state;

		const classHidden = getCN(
			'dropdown-menu',
			'dropdown-menu-indicator-start',
			'dropdown-menu-right',
			{
				show
			}
		);

		return (
			<div
				className="dropdown dropdown-action result-dropdown"
				ref={this.setWrapperRef}
			>
				<ClayButton
					aria-expanded="false"
					aria-haspopup="true"
					className="component-action dropdown-toggle"
					data-toggle="dropdown"
					iconName="ellipsis-v"
					id="optionDropdown"
					onClick={this._handleDropdownToggle}
				/>

				<ul aria-labelledby="optionDropdown" className={classHidden}>
					{onClickPin && (
						<li>
							<ClayButton
								className="dropdown-item"
								displayStyle="link"
								onClick={this._handleDropdownAction(onClickPin)}
								size="sm"
							>
								<div className="dropdown-item-indicator">
									<ClayIcon
										iconName={pinned ? 'unpin' : 'pin'}
									/>
								</div>

								{pinned ?
									getPluralMessage(
										Liferay.Language.get('unpin-result'),
										Liferay.Language.get('unpin-results'),
										itemCount
									) :
									getPluralMessage(
										Liferay.Language.get('pin-result'),
										Liferay.Language.get('pin-results'),
										itemCount
									)
								}
							</ClayButton>
						</li>
					)}

					{!addedResult && onClickHide && (
						<li>
							<ClayButton
								className="dropdown-item"
								displayStyle="link"
								onClick={this._handleDropdownAction(onClickHide)}
								size="sm"
							>
								<div className="dropdown-item-indicator">
									<ClayIcon iconName={hidden ? 'view' : 'hidden'} />
								</div>

								{hidden ?
									getPluralMessage(
										Liferay.Language.get('show-result'),
										Liferay.Language.get('show-results'),
										itemCount
									) :
									getPluralMessage(
										Liferay.Language.get('hide-result'),
										Liferay.Language.get('hide-results'),
										itemCount
									)
								}
							</ClayButton>
						</li>
					)}
				</ul>
			</div>
		);
	}
}

export default Dropdown;