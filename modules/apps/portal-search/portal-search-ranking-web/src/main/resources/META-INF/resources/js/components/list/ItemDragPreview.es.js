import Item from './Item.es';
import React from 'react';

const DRAG_PREVIEW_STYLES = {
	borderWidth: 0,
	borderRadius: '2px',
	boxShadow: '0 0 0 0.125rem #FFF, 0 0 0 0.25rem #80ACFF, 0px 8px 16px rgba(39, 40, 51, 0.16)',
	fontSize: '14px',
	maxWidth: '800px'
};

const ItemDragPreview = props => (
	<Item.DecoratedComponent style={DRAG_PREVIEW_STYLES} {...props} />
);

export default ItemDragPreview;