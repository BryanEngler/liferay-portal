import ClayButton from 'components/shared/ClayButton.es';
import ClayEmptyState from 'components/shared/ClayEmptyState.es';
import DragLayer from './DragLayer.es';
import HTML5Backend from 'react-dnd-html5-backend';
import Item from './Item.es';
import React, {Component} from 'react';
import SearchBar from './SearchBar.es';
import {DragDropContext as dragDropContext} from 'react-dnd';
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

	_handleDragHover = index => {
		this.setState({hoverIndex: index});
	};

	_handleLoadMoreResults = () => {
		this.props.onLoadResults();
	};

	/**
	 * Passes along the same arguments from the onClickHide prop, but also
	 * removes itself from the selected ids list.
	 */
	_handleRemoveSelect = ids => {
		this.setState(
			state => (
				{selectedIds: state.selectedIds.filter(id => !ids.includes(id))}
			)
		);
	}

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
		const {dataMap, onClickHide, onClickPin, onMove} = this.props;

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
				onClickHide={onClickHide}
				onClickPin={onClickPin}
				onDragHover={this._handleDragHover}
				onMove={onMove}
				onRemoveSelect={this._handleRemoveSelect}
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
			onClickHide,
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
					onClickHide={onClickHide}
					onClickPin={onClickPin}
					onRemoveSelect={this._handleRemoveSelect}
					onSearchBarEnter={onSearchBarEnter}
					onSelectAll={this._handleSelectAll}
					onSelectClear={this._handleSelectClear}
					onUpdateSearchBarTerm={onUpdateSearchBarTerm}
					resultIds={resultIds}
					searchBarTerm={searchBarTerm}
					selectedIds={selectedIds}
				/>

				{!!resultIds.length && (
					<ul className="list-group">
						{resultIds.map(
							(id, index, arr) =>
								this._renderItem(id, index, arr)
						)}
					</ul>
				)}

				{dataLoading && (
					<div className="load-more-container">
						<span className="loading-animation" />
					</div>
				)}

				{!dataLoading &&
					<React.Fragment>
						{!resultIds.length && <ClayEmptyState />}

						{this._hasMoreData() && (
							<div className="load-more-container">
								<ClayButton
									className="load-more-button"
									label={Liferay.Language.get('load-more-results')}
									onClick={this._handleLoadMoreResults}
								/>
							</div>
						)}
					</React.Fragment>
				}
			</div>
		);
	}
}

export default dragDropContext(HTML5Backend)(List);