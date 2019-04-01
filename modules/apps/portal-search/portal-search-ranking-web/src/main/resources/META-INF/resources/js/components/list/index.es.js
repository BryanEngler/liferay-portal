import ClayButton from '../shared/ClayButton.es';
import DragLayer from './DragLayer.es';
import HTML5Backend from 'react-dnd-html5-backend';
import Item from './Item.es';
import React, {Component} from 'react';
import SearchBar from './SearchBar.es';
import {DragDropContext as dragDropContext} from 'react-dnd';
import {getLang} from '../../utils/language.es';
import {PropTypes} from 'prop-types';
import {toggleListItem} from '../../utils/util.es';

class List extends Component {
	static propTypes = {
		dataLoading: PropTypes.bool,
		dataMap: PropTypes.object,
		onAddResultSubmit: PropTypes.func,
		onClickHide: PropTypes.func,
		onClickPin: PropTypes.func,
		onLoadResults: PropTypes.func,
		onMove: PropTypes.func,
		onSearchBarEnter: PropTypes.func,
		onUpdateSearchBarTerm: PropTypes.func,
		resultIds: PropTypes.arrayOf(Number),
		searchBarTerm: PropTypes.string,
		totalResultsCount: PropTypes.number
	};

	static defaultProps = {
		dataLoading: false,
		resultIds: [],
		resultIdsHidden: [],
		resultIdsPinned: []
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
		this.setState(
			state => (
				{selectedIds: state.selectedIds.filter(id => !ids.includes(id))}
			)
		);

		this.props.onClickHide(ids, hide);
	};

	_handleDragHover = index => {
		this.setState({hoverIndex: index});
	};

	_handleLoadMoreResults = () => {
		this.props.onLoadResults();
	};

	_handleSelect = id => {
		this.setState(
			state => (
				{selectedIds: toggleListItem(state.selectedIds, id)}
			)
		);
	};

	/**
	 * Clears the selected items. Useful for clearing the selection after
	 * hiding items.
	 */
	_handleSelectClear = () => {
		this.setState({selectedIds: []});
	};

	_handleSelectAll = () => {
		this.setState({selectedIds: this.props.resultIds});
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
				addedResult={item.addedResult}
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
			dataLoading,
			dataMap,
			onAddResultSubmit,
			onClickPin,
			onSearchBarEnter,
			onUpdateSearchBarTerm,
			resultIds,
			searchBarTerm
		} = this.props;

		const {selectedIds} = this.state;

		return (
			<div className="results-ranking-list-root">
				<DragLayer />

				<SearchBar
					dataMap={dataMap}
					onAddResultSubmit={onAddResultSubmit}
					onClickHide={this._handleClickHide}
					onClickPin={onClickPin}
					onSearchBarEnter={onSearchBarEnter}
					onSelectAll={this._handleSelectAll}
					onSelectClear={this._handleSelectClear}
					onUpdateSearchBarTerm={onUpdateSearchBarTerm}
					resultIds={resultIds}
					searchBarTerm={searchBarTerm}
					selectedIds={selectedIds}
				/>

				<ul className="list-group">
					{resultIds.map(
						(id, index, arr) =>
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