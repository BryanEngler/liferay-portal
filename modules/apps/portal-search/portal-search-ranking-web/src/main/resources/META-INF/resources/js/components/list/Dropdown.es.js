import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import getCN from 'classnames';
import ClayButton from '../ClayButton.es';
import ClayIcon from '../ClayIcon.es';
import {getLang} from 'utils/language.es';

class Dropdown extends Component {
	static defaultProps = {
		singular: true
	};

	static propTypes = {
		hidden: PropTypes.bool,
		onClickHide: PropTypes.func,
		onClickPin: PropTypes.func,
		pinned: PropTypes.bool,
		singular: PropTypes.bool
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
			this.setState({
				show: false
			});
		}
	};

	_handleDropdownToggle = event => {
		event.preventDefault();

		this.setState(state => ({
			show: !state.show
		}));
	};

	render() {
		const show = this.state.show;

		const {hidden, onClickHide, onClickPin, pinned, singular} = this.props;

		const classHidden = getCN('dropdown-menu', 'dropdown-menu-right', {
			show: show
		});

		const addPlural = singular ? 'result' : 'results';

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
					id="optionDropdown"
					onClick={this._handleDropdownToggle}
					iconName="ellipsis-v"
				/>

				<ul aria-labelledby="optionDropdown" className={classHidden}>
					{!hidden && (
						<li>
							<a
								className="dropdown-item"
								href="#1"
								onClick={onClickPin}
							>
								<ClayIcon iconName="lock" />

								<span className="dropdown-option-text">
									{pinned
										? getLang(`unpin-${addPlural}`)
										: getLang(`pin-${addPlural}`)}
								</span>
							</a>
						</li>
					)}

					<li>
						<a
							className="dropdown-item"
							href="#1"
							onClick={onClickHide}
						>
							<ClayIcon iconName="hidden" />

							<span className="dropdown-option-text">
								{hidden
									? getLang(`show-${addPlural}`)
									: getLang(`hide-${addPlural}`)}
							</span>
						</a>
					</li>
				</ul>
			</div>
		);
	}
}

export default Dropdown;
