import * as React from 'react';
import {DragLayer as DndDragLayer} from 'react-dnd';
import DRAG_TYPES from 'utils/drag-types.es';
import ItemDragPreview from './ItemDragPreview.es';

const LAYER_STYLES = {
	height: '100%',
	left: 0,
	pointerEvents: 'none',
	position: 'fixed',
	top: 0,
	width: '100%',
	zIndex: 100
};

const OFFSET_X = 8;

const OFFSET_Y = 16;

function getItemStyles(props) {
	const {initialOffset, currentOffset} = props;

	const {x, y} = currentOffset || {};

	const transform = `translate(${x - OFFSET_X}px, ${y - OFFSET_Y}px)`;

	return !initialOffset || !currentOffset
		? {
				display: 'none'
		  }
		: {
				transform,
				WebkitTransform: transform
		  };
}

const DragLayer = props => {
	const {item, itemType, isDragging} = props;

	function renderItem() {
		return itemType === DRAG_TYPES.LIST_ITEM ? (
			<ItemDragPreview {...item} />
		) : null;
	}

	return isDragging ? (
		<div className="drag-layer" style={LAYER_STYLES}>
			<div style={getItemStyles(props)}>{renderItem()}</div>
		</div>
	) : null;
};

export default DndDragLayer(monitor => ({
	currentOffset: monitor.getSourceClientOffset(),
	item: monitor.getItem(),
	itemType: monitor.getItemType(),
	initialOffset: monitor.getInitialSourceClientOffset(),
	isDragging: monitor.isDragging()
}))(DragLayer);
