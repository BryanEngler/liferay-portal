import React, {Component} from 'react';
import Alias from './Alias.es';
import {getLang} from 'utils/language.es';
import List from './list/index.es';
import {getMockResultsData} from 'mock-data.js';
import PageToolbar from './PageToolbar.es';
import {PropTypes} from 'prop-types';
import {
	resultsDataToMap,
	move,
	removeIdFromList,
	updateDataMap
} from 'utils/util.es';
import {
	ClayTabs,
	ClayTab,
	ClayTabList,
	ClayTabPanel
} from 'components/ClayTabs.es';
import DragLayer from './list/DragLayer.es';

class ResultsRankingForm extends Component {
	static propTypes = {
		searchTerm: PropTypes.string.isRequired
	};

	state = {
		addResultIds: [],
		aliases: ['one', 'two', 'three'],
		dataLoading: false,

		/**
		 * Map of all the data. Key is the ID and value is the data object.
		 */
		dataMap: {},

		/**
		 * A full list of IDs which include hidden and pinned items.
		 */
		resultIds: [],

		/**
		 * The list of IDs that are currently hidden.
		 */
		resultIdsHidden: [],

		/**
		 * The list of IDs that are currently pinned.
		 */
		resultIdsPinned: [],
		totalResultsHiddenCount: 0,
		totalResultsVisibleCount: 0
	};

	constructor(props) {
		super(props);

		this._initialResultIdsHidden = [];
		this._initialResultIdsPinned = [];
	}

	componentDidMount() {
		this._fetchResultsData();
		this._fetchResultsDataHidden();
	}

	/**
	 * Retrieves results data from a search term. This will also handle loading
	 * more data to the results list.
	 * @TODO
	 * - Swap out mock data
	 * - Remove simulated loading with setTimeout
	 */
	_fetchResultsData = () => {
		const {resultIds} = this.state;

		this.setState({dataLoading: true});

		setTimeout(() => {
			const dataResponse = getMockResultsData(10, resultIds.length, 100, {
				hidden: false
			});

			const mappedData = resultsDataToMap(dataResponse.data);

			const pinnedIds = dataResponse.data
				.filter(({pinned}) => pinned)
				.map(({id}) => id);

			this._initialResultIdsPinned = [
				...this._initialResultIdsPinned,
				...pinnedIds
			];

			const ids = dataResponse.data.map(({id}) => id);

			this.setState(state => ({
				dataMap: {...state.dataMap, ...mappedData},
				dataLoading: false,
				resultIds: [...state.resultIds, ...ids],
				resultIdsPinned: [...state.resultIdsPinned, ...pinnedIds],
				totalResultsVisibleCount: dataResponse.items
			}));
		}, 2000);
	};

	/**
	 * Retrieves only the hidden data. This is used for showing hidden results
	 * in the hidden tab.
	 * @TODO
	 * - Swap out mock data
	 */
	_fetchResultsDataHidden = (startIndex = 0) => {
		const dataResponse = getMockResultsData(10, startIndex, 400, {
			hidden: true
		});

		const mappedData = resultsDataToMap(dataResponse.data);

		const ids = dataResponse.data.map(({id}) => id);

		this._initialResultIdsHidden = [
			...this._initialResultIdsHidden,
			...ids
		];

		this.setState(state => ({
			dataMap: {...state.dataMap, ...mappedData},
			resultIdsHidden: [...state.resultIdsHidden, ...ids]
		}));
	};

	/**
	 * Gets the visible data results to show in the visible tab. Organizes the
	 * pinned results on to top first and then the remaining unpinned and not
	 * hidden results.
	 */
	_getResultIdsVisible = () => {
		const {resultIdsPinned, resultIds} = this.state;

		const notPinnedOrHiddenIds = resultIds.filter(id =>
			this._isNotPinnedOrHidden(id)
		);

		return [...resultIdsPinned, ...notPinnedOrHiddenIds];
	};

	/**
	 * Handles what happens when an item is pinned or unpinned. Updates the
	 * dataMap and adds the id to the resultsDataPinned list.
	 * @param {array} ids The list of ids to pin.
	 * @param {boolean} pin The new pin value to set. Defaults to true.
	 */
	_handleClickPin = (ids, pin = true) => {
		this.setState(state => ({
			dataMap: updateDataMap(state.dataMap, ids, {pinned: pin}),
			resultIdsPinned: pin
				? [...state.resultIdsPinned, ...ids]
				: removeIdFromList(state.resultIdsPinned, ids)
		}));
	};

	/**
	 * Handles what happens when an item is hidden or shown. Updates the item
	 * on the dataMap and moves the id between the visible and hidden lists.
	 * Also unpins the item when hiding a pinned item.
	 * @param {array} ids The list of ids to hide or show.
	 * @param {boolean} hide The new hide value to set. Defaults to true.
	 */
	_handleClickHide = (ids, hide = true) => {
		this.setState(state => ({
			dataMap: updateDataMap(state.dataMap, ids, {
				hidden: hide,
				pinned: false
			}),
			resultIdsHidden: hide
				? [...ids, ...state.resultIdsHidden]
				: removeIdFromList(state.resultIdsHidden, ids),
			resultIdsPinned: hide
				? removeIdFromList(state.resultIdsPinned, ids)
				: state.resultIdsPinned
		}));
	};

	/**
	 * Handles reordering an item in a list. This will update the results list.
	 * @param {number} fromIndex The index of the item that is being moved.
	 * @param {number} toIndex The new index that the item will be moved to.
	 */
	_handleMove = (fromIndex, toIndex) => {
		this.setState(state => ({
			resultIdsPinned: move(state.resultIdsPinned, fromIndex, toIndex)
		}));
	};

	/**
	 * Handles adding to the alias list and filters out duplicate words.
	 * @param {array} value The value of the new aliases (array of String).
	 */
	_handleUpdateAlias = value => {
		this.setState(state => ({
			aliases: [
				...state.aliases,
				...value.filter(item => !state.aliases.includes(item))
			]
		}));
	};

	/**
	 * Handles updating the array of added result ids after 'Add a Result'
	 * submission.
	 * @param {array} addedResultsDataList The value of the added results
	 * (array of objects).
	 */
	_handleUpdateAddResultIds = addedResultsDataList => {
		const mappedData = resultsDataToMap(addedResultsDataList);

		const newMappedData = updateDataMap(
			mappedData,
			addedResultsDataList.map(({id}) => id),
			{
				pinned: true
			}
		);

		this.setState(state => ({
			dataMap: {...state.dataMap, ...newMappedData},
			resultIdsPinned: [
				...addedResultsDataList
					.filter(
						result => !state.resultIdsPinned.includes(result.id)
					)
					.map(({id}) => id),
				...state.resultIdsPinned
			]
		}));
	};

	/**
	 * Handles removing an alias.
	 * @param {String} label Removes the alias with given label.
	 */
	_handleRemoveAlias = label => {
		this.setState(state => ({
			aliases: state.aliases.filter(item => item !== label)
		}));
	};

	/**
	 * Checks if an item is neither pinned or hidden. Useful for displaying
	 * the remaining results in the visible tab.
	 * @param {number|string} id The id of the item to check.
	 */
	_isNotPinnedOrHidden = id => {
		const {resultIdsHidden, resultIdsPinned} = this.state;

		return !resultIdsPinned.includes(id) && !resultIdsHidden.includes(id);
	};

	render() {
		const {searchTerm} = this.props;

		const {
			aliases,
			dataMap,
			dataLoading,
			resultIdsHidden,
			selected,
			totalResultsHiddenCount,
			totalResultsVisibleCount
		} = this.state;

		return (
			<div className="results-ranking-form">
				<PageToolbar />

				<div className="container-fluid container-fluid-max-xl container-form-lg">
					<Alias
						keywords={aliases}
						onClickDelete={this._handleRemoveAlias}
						onClickSubmit={this._handleUpdateAlias}
						searchTerm={searchTerm}
					/>

					<div className="sheet sheet-lg">
						<div className="sheet-text">
							<strong>{getLang('results')}</strong>
						</div>

						<div className="results-ranking-list">
							<DragLayer />

							<ClayTabs onSelect={this._handleTabSelect}>
								<ClayTabList className="results-ranking-tabs">
									<ClayTab>{getLang('visible')}</ClayTab>

									<ClayTab>{getLang('hidden')}</ClayTab>
								</ClayTabList>

								<ClayTabPanel>
									<List
										dataMap={dataMap}
										dataLoading={dataLoading}
										onAddResultSubmit={
											this._handleUpdateAddResultIds
										}
										onClickHide={this._handleClickHide}
										onClickPin={this._handleClickPin}
										onLoadResults={this._fetchResultsData}
										onMove={this._handleMove}
										resultIds={this._getResultIdsVisible()}
										selected={selected}
										totalResultsCount={
											totalResultsVisibleCount
										}
									/>
								</ClayTabPanel>

								<ClayTabPanel>
									<List
										dataMap={dataMap}
										dataLoading={dataLoading}
										onClickHide={this._handleClickHide}
										onLoadResults={
											this._fetchResultsDataHidden
										}
										resultIds={resultIdsHidden}
										selected={selected}
										totalResultsCount={
											totalResultsHiddenCount
										}
									/>
								</ClayTabPanel>
							</ClayTabs>
						</div>
					</div>
				</div>
			</div>
		);
	}
}

export default ResultsRankingForm;
