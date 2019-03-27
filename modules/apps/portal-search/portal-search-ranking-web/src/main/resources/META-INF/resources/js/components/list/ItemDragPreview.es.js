import React from 'react';
import Item from './Item.es';

const DRAG_PREVIEW_STYLES = {
	border: '2px solid #80ACFF',
	borderRadius: '4px',
	boxShadow: '0px 8px 16px rgba(39, 40, 51, 0.16)',
	fontSize: '14px',
	maxWidth: '800px'
};

const ItemDragPreview = props => (
	<Item.DecoratedComponent style={DRAG_PREVIEW_STYLES} {...props} />
);

export default ItemDragPreview;
