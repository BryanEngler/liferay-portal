import ClayButton from '../ClayButton.es';
import ClayIcon from '../ClayIcon.es';
import getCN from 'classnames';
import React, {Component} from 'react';
import {getLang} from 'utils/language.es';
import {PropTypes} from 'prop-types';

class Dropdown extends Component {
	static propTypes = {
		hidden: PropTypes.bool,
		onClickHide: PropTypes.func,
		onClickPin: PropTypes.func,
		pinned: PropTypes.bool,
		singular: PropTypes.bool
	};

	static defaultProps = {
		singular: true
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
		const show = this.state.show;

		const {hidden, onClickHide, onClickPin, pinned, singular} = this.props;

		const classHidden = getCN(
			'dropdown-menu',
			'dropdown-menu-right',
			{
				show
			}
		);

		const resultLabel = singular ? 'result' : 'results';

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
					{!hidden && (
						<li>
							<a
								className="dropdown-item"
								href="#"
								onClick={this._handleDropdownAction(onClickPin)}
							>
								<ClayIcon iconName="lock" />

								<span className="dropdown-option-text">
									{pinned ?
										getLang(`unpin-${resultLabel}`) :
										getLang(`pin-${resultLabel}`)}
								</span>
							</a>
						</li>
					)}

					{onClickHide && (
						<li>
							<a
								className="dropdown-item"
								href="#"
								onClick={this._handleDropdownAction(onClickHide)}
							>
								<ClayIcon iconName="hidden" />

								<span className="dropdown-option-text">
									{hidden ?
										getLang(`show-${resultLabel}`) :
										getLang(`hide-${resultLabel}`)}
								</span>
							</a>
						</li>
					)}
				</ul>
			</div>
		);
	}
}

export default Dropdown;