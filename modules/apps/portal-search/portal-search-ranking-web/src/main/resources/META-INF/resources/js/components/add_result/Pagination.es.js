import ClayIcon from 'components/shared/ClayIcon.es';
import getCN from 'classnames';
import isFunction from 'lodash.isfunction';
import React, {Component} from 'react';
import times from 'lodash.times';
import {getLang} from 'utils/language.es';
import {PropTypes} from 'prop-types';

const DEFAULT_PAGE = 1;

const PAGE_BUFFER = 1;

class PaginationEllipsisItem extends React.Component {
	static propTypes = {
		href: PropTypes.string,
		onChange: PropTypes.func,
		page: PropTypes.number
	};

	_handleChange = () => {
		const {onChange, page} = this.props;

		if (onChange) {
			onChange(page);
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
				{this.props.children}
			</a>
		);
	}
}

class PaginationEllipsis extends React.Component {
	static propTypes = {
		href: PropTypes.string,
		items: PropTypes.array,
		onChange: PropTypes.func
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

		this.setState(state => ({show: !state.show}));
	};

	render() {
		const {href, items, onChange} = this.props;

		const {show} = this.state;

		const classHidden = getCN(
			'dropdown-menu',
			'dropdown-menu-top-center',
			{
				show
			}
		);

		return (
			<div className="dropdown page-item" ref={this.setWrapperRef}>
				<a
					aria-expanded="false"
					aria-haspopup="true"
					className="dropdown-toggle page-link"
					data-toggle="dropdown"
					href={href}
					onClick={this._handleDropdownToggle}
					role="button"
				>
					{'...'}
				</a>

				<ul className={classHidden}>
					{items.map(
						item => (
							<PaginationEllipsisItem
								href={href}
								key={item}
								onChange={onChange}
								page={item}
							>
								{item}
							</PaginationEllipsisItem>
						)
					)}
				</ul>
			</div>
		);
	}
}

class PaginationItem extends React.Component {
	static defaultProps = {
		active: false,
		disabled: false
	};

	static propTypes = {
		active: PropTypes.bool,
		disabled: PropTypes.bool,
		href: PropTypes.string,
		onChange: PropTypes.func,
		page: PropTypes.oneOfType([PropTypes.number, PropTypes.element])
			.isRequired
	};

	_handleChange = () => {
		const {onChange, page} = this.props;

		if (onChange) {
			onChange(page);
		}
	};

	render() {
		const {active, children, disabled, href, page} = this.props;

		const classes = getCN(
			'page-item',
			{
				active,
				disabled
			}
		);

		return (
			<li className={classes}>
				{page >= 0 ? (
					<a
						className="page-link"
						href={href}
						onClick={this._handleChange}
					>
						{children}
					</a>
				) : (
					children
				)}
			</li>
		);
	}
}

class Pagination extends Component {
	static defaultProps = {
		page: DEFAULT_PAGE
	};

	static propTypes = {
		href: PropTypes.string,
		onChange: PropTypes.func,
		page: PropTypes.number,
		total: PropTypes.number
	};

	_handlePageChangePrevious = () => {
		if (this.props.page > 1) {
			this.props.onChange(this.props.page - 1);
		}
	};

	_handlePageChangeNext = () => {
		if (this.props.page < this.props.total) {
			this.props.onChange(this.props.page + 1);
		}
	};

	getPages() {
		const {href, onChange, page, total} = this.props;

		const pages = times(total, i => i + 1);

		const frontBuffer = page - PAGE_BUFFER;

		if (frontBuffer > 1) {
			const start = 1;

			const numOfItems = frontBuffer - 2;

			const removedItems = pages.slice(start, start + numOfItems);

			if (removedItems.length) {
				pages.splice(
					start,
					numOfItems,
					<PaginationEllipsis
						href={href}
						items={removedItems}
						key="paginationEllipsis1"
						onChange={onChange}
					/>
				);
			}
		}

		const backBuffer = page + PAGE_BUFFER;

		if (backBuffer < total) {
			const start = pages.indexOf(backBuffer + 1);

			const numOfItems = total - backBuffer - 1;

			const removedItems = pages.slice(start, start + numOfItems);

			if (removedItems.length) {
				pages.splice(
					start,
					numOfItems,
					<PaginationEllipsis
						href={href}
						items={removedItems}
						key="paginationEllipsis2"
						onChange={onChange}
					/>
				);
			}
		}

		return pages;
	}

	render() {
		const {href, onChange, page, total} = this.props;

		const classPrevious = getCN(
			'page-item',
			{disabled: page === 1}
		);

		const classNext = getCN(
			'page-item',
			{disabled: page === total}
		);

		return (
			<ul className="pagination">
				<li className={classPrevious}>
					<a
						className="page-link"
						href={href}
						onClick={this._handlePageChangePrevious}
						role="button"
						tabIndex="-1"
					>
						<ClayIcon iconName="angle-left" />

						<span className="sr-only">{getLang('previous')}</span>
					</a>
				</li>

				{this.getPages().map(
					(item, index) => (
						<PaginationItem
							active={page === item}
							href={href}
							key={index}
							onChange={onChange}
							page={isFunction(item) ? -1 : item}
						>
							{item}
						</PaginationItem>
					)
				)}

				<li className={classNext}>
					<a
						className="page-link"
						href={href}
						onClick={this._handlePageChangeNext}
						role="button"
					>
						<ClayIcon iconName="angle-right" />

						<span className="sr-only">{getLang('next')}</span>
					</a>
				</li>
			</ul>
		);
	}
}

export default Pagination;