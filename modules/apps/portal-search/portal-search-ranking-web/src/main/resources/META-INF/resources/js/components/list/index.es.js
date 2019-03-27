import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import HTML5Backend from 'react-dnd-html5-backend';
import {DragDropContext as dragDropContext} from 'react-dnd';
import Item from './Item.es';
import SearchBar from './SearchBar.es';
import {getLang} from '../../utils/language.es';
import {toggleListItem} from '../../utils/util.es';
import ClayButton from '../ClayButton.es';

class List extends Component {
	static propTypes = {
		dataMap: PropTypes.object,
		dataLoading: PropTypes.bool,
		resultIds: PropTypes.arrayOf(Number),
		onAddResultSubmit: PropTypes.func,
		onClickHide: PropTypes.func,
		onClickPin: PropTypes.func,
		onLoadResults: PropTypes.func,
		onMove: PropTypes.func,
		totalResultsCount: PropTypes.number
	};

	static defaultProps = {
		dataLoading: false,
		resultIdsHidden: [],
		resultIdsPinned: [],
		resultIds: []
	};

	state = {
		hoverIndex: null,
		selectedIds: []
	};

	/**
	 * Passes along the same arguments from the onClickHide prop, but also
	 * removes itself from the selected ids list.
	 */
	_handleClickHide = (ids, hide) => {
		this.setState(state => ({
			selectedIds: state.selectedIds.filter(id => !ids.includes(id))
		}));

		this.props.onClickHide(ids, hide);
	};

	_handleDragHover = index => {
		this.setState({hoverIndex: index});
	};

	_handleLoadMoreResults = () => {
		this.props.onLoadResults();
	};

	_handleSelect = id => {
		this.setState(state => ({
			selectedIds: toggleListItem(state.selectedIds, id)
		}));
	};

	/**
	 * Clears the selected items. Useful for clearing the selection after
	 * hiding items.
	 */
	_handleSelectClear = () => {
		this.setState({selectedIds: []});
	};

	_handleSelectAll = () => {
		const {resultIds} = this.props;

		this.setState(state => ({
			selectedIds:
				state.selectedIds.length === resultIds.length ? [] : resultIds
		}));
	};

	_handleTabSelect = (index, lastIndex) => {
		if (index !== lastIndex) {
			this.setState({selectedIds: []});
		}
	};

	_hasMoreData = () => {
		const {resultIds, totalResultsCount} = this.props;

		return resultIds.length < totalResultsCount;
	};

	_renderItem = (id, index, arr) => {
		const {dataMap, onClickPin, onMove} = this.props;

		const {selectedIds} = this.state;

		const item = dataMap[id];

		return (
			<Item
				author={item.author}
				clicks={item.clicks}
				date={item.date}
				description={item.description}
				extension={item.extension}
				hidden={item.hidden}
				hoverIndex={this.state.hoverIndex}
				id={item.id}
				index={index}
				key={item.id}
				lastIndex={arr.length}
				onClickHide={this._handleClickHide}
				onClickPin={onClickPin}
				onDragHover={this._handleDragHover}
				onMove={onMove}
				onSelect={this._handleSelect}
				pinned={item.pinned}
				selected={selectedIds.includes(item.id)}
				title={item.title}
				type={item.type}
			/>
		);
	};

	render() {
		const {
			dataMap,
			dataLoading,
			onAddResultSubmit,
			onClickPin,
			resultIds
		} = this.props;

		const {selectedIds} = this.state;

		return (
			<div className="results-ranking-list-root">
				<SearchBar
					dataMap={dataMap}
					onAddResultSubmit={onAddResultSubmit}
					onClickHide={this._handleClickHide}
					onClickPin={onClickPin}
					onSelectAll={this._handleSelectAll}
					onSelectClear={this._handleSelectClear}
					resultIds={resultIds}
					selectedIds={selectedIds}
				/>

				<ul className="list-group">
					{resultIds.map((id, index, arr) =>
						this._renderItem(id, index, arr)
					)}
				</ul>

				{dataLoading && (
					<div className="load-more-container">
						<span className="loading-animation" />
					</div>
				)}

				{!dataLoading && this._hasMoreData() && (
					<div className="load-more-container">
						<ClayButton
							className="load-more-button"
							label={getLang('load-more-results')}
							onClick={this._handleLoadMoreResults}
						/>
					</div>
				)}
			</div>
		);
	}
}

export default dragDropContext(HTML5Backend)(List);
