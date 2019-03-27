import React, {Component} from 'react';
import {findDOMNode} from 'react-dom';
import {PropTypes} from 'prop-types';
import {DragSource as dragSource, DropTarget as dropTarget} from 'react-dnd';
import {getEmptyImage} from 'react-dnd-html5-backend';
import DRAG_TYPES from 'utils/drag-types.es';
import {getLang, sub} from 'utils/language.es';
import ClayIcon from '../ClayIcon.es';
import Dropdown from './Dropdown.es';
import getCN from 'classnames';

/**
 * Passes the required values to the drop target and drag preview.
 * This method must be called `beginDrag`.
 * @param {Object} props Component's current props
 * @returns {Object} The props to be passed to the drop target and drag preview.
 */
function beginDrag({
	author,
	clicks,
	date,
	description,
	extension,
	hidden,
	hoverIndex,
	id,
	index,
	lastIndex,
	pinned,
	selected,
	title,
	type
}) {
	return {
		author,
		clicks,
		date,
		description,
		extension,
		hidden,
		hoverIndex,
		id,
		index,
		lastIndex,
		pinned,
		selected,
		title,
		type
	};
}

/**
 * Prevents dropping from the same position.
 * This method must be called `canDrop`.
 * @param {Object} props Component's current props.
 * @param {DropTargetMonitor} monitor
 * @returns {boolean} True if the target should accept the item.
 */
function canDrop(props, monitor) {
	const {hoverIndex, pinned} = props;

	const {index: itemIndex} = monitor.getItem();

	return pinned && itemIndex !== hoverIndex && itemIndex + 1 !== hoverIndex;
}

/**
 * Implements the behavior of what will occur when an item stops being dragged.
 * Moves the dragged item to the dropped index.
 * This method must be called `endDrag`.
 * @param {Object} props The current props of the component being dragged.
 * @param {DropTargetMonitor} monitor
 */
function endDrag(props, monitor) {
	const {hoverIndex, onMove} = props;

	const {index: itemIndex} = monitor.getItem();

	if (monitor.didDrop()) {
		onMove(itemIndex, hoverIndex);
	}

	props.onDragHover(null);
}

/**
 * Updates the hover indicator line.
 * @param {Object} props The component's current props.
 * @param {DropTargetMonitor} monitor
 * @param {DragDropContainer} component The component being hovered over.
 */
function hover(props, monitor, component) {
	const {index, onDragHover} = props;

	if (isHoverAbove(monitor, component)) {
		onDragHover(index);
	} else {
		onDragHover(index + 1);
	}
}

/**
 * A helper method for drag and drop methods.
 * Checks if the mouse is hovering over an item's top-half.
 * @param {DropTargetMonitor} monitor
 * @param {DragDropContainer} component The component being hovered over.
 */
function isHoverAbove(monitor, component) {
	// Determine rectangle on screen
	const hoverBoundingRect = findDOMNode(component).getBoundingClientRect();

	// Get vertical middle
	const hoverMiddleY = (hoverBoundingRect.bottom - hoverBoundingRect.top) / 2;

	// Determine mouse position
	const clientOffset = monitor.getClientOffset();

	// Get pixels to the top
	const hoverClientY = clientOffset.y - hoverBoundingRect.top;

	return hoverClientY < hoverMiddleY;
}

const DND_PROPS = {
	canDrop: PropTypes.bool,
	connectDragPreview: PropTypes.func,
	connectDragSource: PropTypes.func,
	connectDropTarget: PropTypes.func,
	dragging: PropTypes.bool,
	hovering: PropTypes.bool
};

class Item extends Component {
	static propTypes = {
		...DND_PROPS,
		author: PropTypes.string,
		clicks: PropTypes.number,
		date: PropTypes.string,
		description: PropTypes.string,
		extension: PropTypes.string,
		hidden: PropTypes.bool,
		hoverIndex: PropTypes.number,
		lastIndex: PropTypes.number,
		id: PropTypes.number,
		index: PropTypes.number,
		initialPinned: PropTypes.number,
		onClickHide: PropTypes.func,
		onClickPin: PropTypes.func,
		onDragHover: PropTypes.func,
		onMove: PropTypes.func,
		onSelect: PropTypes.func,
		pinned: PropTypes.bool,
		searchTerm: PropTypes.string,
		selected: PropTypes.bool,
		title: PropTypes.string,
		type: PropTypes.string
	};

	static defaultProps = {
		connectDragPreview: val => val,
		connectDragSource: val => val,
		connectDropTarget: val => val
	};

	/**
	 * Use empty image as a drag preview so browsers don't draw it and
	 * we can draw what we want on the custom drag layer instead.
	 *
	 * captureDraggingState: true for IE fallback. This specifies that we'd
	 * rather screenshot the node when it already knows it's being dragged so we
	 * can hide it with CSS.
	 */
	componentDidMount() {
		const {connectDragPreview} = this.props;

		if (connectDragPreview) {
			connectDragPreview(getEmptyImage(), {
				captureDraggingState: true
			});
		}
	}

	_handleSelect = () => {
		this.props.onSelect(this.props.id);
	};

	_handlePin = () => {
		this.props.onClickPin([this.props.id], !this.props.pinned);
	};

	_handleHide = () => {
		this.props.onClickHide([this.props.id], !this.props.hidden);
	};

	_renderDescription = () => {
		const {description} = this.props;

		let descriptionBlock = '';

		if (description) {
			const descriptionText =
				description.length > 75
					? `${description.slice(0, 75)}...`
					: description;

			descriptionBlock = (
				<p className="list-group-text list-item-description">
					{descriptionText}
				</p>
			);
		}

		return descriptionBlock;
	};

	render() {
		const {
			author,
			canDrop,
			clicks,
			connectDragSource,
			connectDropTarget,
			dragging,
			date,
			extension,
			hidden,
			hoverIndex,
			index,
			lastIndex,
			onClickHide,
			onClickPin,
			onDragHover,
			pinned,
			selected,
			style,
			title,
			type
		} = this.props;

		const colorScheme = {
			doc: 'blue',
			pdf: 'red',
			png: 'purple'
		};

		const colorSticker = colorScheme[extension]
			? colorScheme[extension]
			: 'grey';

		const classSticker = getCN(
			`icon-${colorSticker}`,
			'result-icon',
			'sticker'
		);

		const listClasses = getCN(
			'list-group-item',
			'list-group-item-flex',
			'results-ranking-item',
			{
				'list-item-drag-hover': canDrop && index === hoverIndex,
				'list-item-drag-hover-below':
					index + 1 === hoverIndex && hoverIndex === lastIndex,
				'list-item-dragging': dragging,
				'results-ranking-item-hidden': hidden,
				'results-ranking-item-pinned': pinned
			}
		);

		return connectDropTarget(
			<li className={listClasses} style={style}>
				{onDragHover && !hidden && (
					<div className="autofit-col result-drag">
						{connectDragSource(
							<span className="result-drag-sticker sticker sticker-secondary">
								<ClayIcon iconName="drag" />
							</span>
						)}
					</div>
				)}

				<div className="autofit-col">
					<div className="custom-control custom-checkbox">
						<label>
							<input
								checked={selected}
								className="custom-control-input"
								onChange={this._handleSelect}
								type="checkbox"
							/>

							<span className="custom-control-label" />
						</label>
					</div>
				</div>

				<div className="autofit-col">
					<span className={classSticker}>
						{extension ? (
							extension.toUpperCase()
						) : (
							<ClayIcon iconName="web-content" />
						)}
					</span>
				</div>

				<div className="autofit-col autofit-col-expand">
					<section className="autofit-section">
						<h4 className="list-group-title">
							<span className="text-truncate-inline">
								<a href="#1">{title}</a>
							</span>
						</h4>

						<p className="list-group-subtext">
							{`${author} - ${date}`}
						</p>

						<p className="list-group-subtext">[{type}]</p>

						{this._renderDescription()}
					</section>
				</div>

				{onClickHide && (
					<div className="autofit-col">
						<div className="result-hide">
							<a
								className="component-action"
								href="#1"
								onClick={this._handleHide}
								role="button"
							>
								<ClayIcon iconName="hidden" />
							</a>
						</div>
					</div>
				)}

				{onClickPin && !hidden && (
					<div className="autofit-col">
						<div className="result-pin">
							<a
								className="component-action"
								href="#1"
								onClick={this._handlePin}
								role="button"
							>
								<ClayIcon iconName="lock" />
							</a>
						</div>
					</div>
				)}

				{onClickPin && onClickHide && (
					<div className="autofit-col">
						<Dropdown
							hidden={hidden}
							onClickHide={this._handleHide}
							onClickPin={this._handlePin}
							pinned={pinned}
						/>
					</div>
				)}

				<div className="click-count list-group-text sticker-bottom-right">
					{sub(
						getLang('clicks-x'),
						[<b key="CLICK_COUNT">{clicks}</b>],
						false
					)}
				</div>
			</li>
		);
	}
}

const ItemWithDrag = dragSource(
	DRAG_TYPES.LIST_ITEM,
	{
		beginDrag,
		endDrag
	},
	(connect, monitor) => ({
		connectDragPreview: connect.dragPreview(),
		connectDragSource: connect.dragSource(),
		dragging: monitor.isDragging()
	})
)(Item);

export default dropTarget(
	DRAG_TYPES.LIST_ITEM,
	{
		canDrop,
		hover
	},
	(connect, monitor) => ({
		canDrop: monitor.canDrop(),
		connectDropTarget: connect.dropTarget()
	})
)(ItemWithDrag);
