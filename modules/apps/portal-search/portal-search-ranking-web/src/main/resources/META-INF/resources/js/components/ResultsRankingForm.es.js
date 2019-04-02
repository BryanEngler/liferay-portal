import Alias from 'components/alias/index.es';
import List from 'components/list/index.es';
import PageToolbar from './PageToolbar.es';
import React, {Component} from 'react';
import ReactModal from 'react-modal';
import {
	ClayTab,
	ClayTabList,
	ClayTabPanel,
	ClayTabs
} from 'components/shared/ClayTabs.es';
import {getMockResultsData} from 'test/mock-data.js';
import {
	move,
	removeIdFromList,
	resultsDataToMap,
	updateDataMap
} from 'utils/util.es';
import {PropTypes} from 'prop-types';

class ResultsRankingForm extends Component {
	static propTypes = {
		cancelUrl: PropTypes.string.isRequired,
		searchTerm: PropTypes.string.isRequired
	};

	state = {
		addResultIds: [],
		aliases: [],
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
		searchBarTerm: '',
		totalResultsHiddenCount: 0,
		totalResultsVisibleCount: 0
	};

	constructor(props) {
		super(props);

		this._initialAliases = this.state.aliases;
		this._initialResultIds = [];
		this._initialResultIdsHidden = [];
		this._initialResultIdsPinned = [];
	}

	componentDidMount() {
		ReactModal.setAppElement('.results-ranking-form-root');

		this._handleFetchResultsData();
		this._handleFetchResultsDataHidden();
	}

	/**
	 * Clears past resultIds, both pinned and hidden lists as a preface for
	 * using the searchbar.
	 */
	_clearResultsData = () => {
		this.setState(
			{
				resultIds: [],
				resultIdsHidden: [],
				resultIdsPinned: [],
				totalResultsHiddenCount: 0,
				totalResultsVisibleCount: 0
			}
		);

		this._initialResultIds = [];
		this._initialResultIdsHidden = [];
		this._initialResultIdsPinned = [];
	};

	/**
	 * Returns a boolean of whether the alias list has changed.
	 */
	_getAliasUnchanged = () =>
		this._initialAliases.length === this.state.aliases.length && this._initialAliases.every(item => this.state.aliases.includes(item));

	/**
	 * Checks whether changes have been made for submission. Checks the lengths of
	 * each hidden/pinned added/removed array and the aliases list.
	 */
	_getDisablePublish = () =>
		this._getAliasUnchanged() && this._getHiddenAdded().length === 0 && this._getHiddenRemoved().length === 0 && this._getPinnedRemoved().length === 0 && this._getPinnedAdded().length === 0;

	/**
	 * Gets the added changes in hidden from the initial and current states.
	 */
	_getHiddenAdded = () =>
		this.state.resultIdsHidden.filter(
			item => !this._initialResultIdsHidden.includes(item)
		);

	/**
	 * Gets the removed changes in hidden from the initial and current states.
	 */
	_getHiddenRemoved = () =>
		this._initialResultIdsHidden.filter(
			item => !this.state.resultIdsHidden.includes(item)
		);

	/**
	 * Gets the removed changes in pinned from the initial and current states.
	 */
	_getPinnedRemoved = () =>
		this._initialResultIdsPinned.filter(
			item => !this.state.resultIdsPinned.includes(item)
		);

	/**
	 * Gets the added changes in pinned from the initial and current states.
	 */
	_getPinnedAdded = () =>
		this.state.resultIdsPinned.filter(
			item => !this._initialResultIdsPinned.includes(item)
		);

	/**
	 * Gets the visible data results to show in the visible tab. Organizes the
	 * pinned results on to top first and then the remaining unpinned and not
	 * hidden results.
	 */
	_getResultIdsVisible = () => {
		const {resultIds, resultIdsPinned} = this.state;

		const notPinnedOrHiddenIds = resultIds.filter(
			id => this._isNotPinnedOrHidden(id)
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
		this.setState(
			state => (
				{
					dataMap: updateDataMap(
						state.dataMap,
						ids,
						{pinned: pin}
					),
					resultIdsPinned: pin ?
						[...state.resultIdsPinned, ...ids] :
						removeIdFromList(state.resultIdsPinned, ids)
				}
			)
		);
	};

	/**
	 * Handles what happens when an item is hidden or shown. Updates the item
	 * on the dataMap and moves the id between the visible and hidden lists.
	 * Also unpins the item when hiding a pinned item.
	 * @param {array} ids The list of ids to hide or show.
	 * @param {boolean} hide The new hide value to set. Defaults to true.
	 */
	_handleClickHide = (ids, hide = true) => {
		this.setState(
			state => (
				{
					dataMap: updateDataMap(
						state.dataMap,
						ids,
						{
							hidden: hide,
							pinned: false
						}
					),
					resultIds: hide ?
						removeIdFromList(state.resultIds, ids) :
						[...state.resultIds, ...ids],
					resultIdsHidden: hide ?
						[...ids, ...state.resultIdsHidden] :
						removeIdFromList(state.resultIdsHidden, ids),
					resultIdsPinned: hide ?
						removeIdFromList(state.resultIdsPinned, ids) :
						state.resultIdsPinned
				}
			)
		);
	};

	/**
	 * Retrieves results data from a search term. This will also handle loading
	 * more data to the results list.
	 * @TODO
	 * - Swap out mock data
	 * - Remove simulated loading with setTimeout
	 */
	_handleFetchResultsData = () => {
		this.setState({dataLoading: true});

		setTimeout(
			() => {
				const dataResponse = getMockResultsData(
					10,
					this._initialResultIds.length,
					100,
					this.state.searchBarTerm,
					{
						hidden: false
					}
				);

				const mappedData = resultsDataToMap(dataResponse.data);

				const pinnedIds = dataResponse.data
					.filter(({pinned}) => pinned)
					.map(({id}) => id);

				this._initialResultIdsPinned = [
					...this._initialResultIdsPinned,
					...pinnedIds
				];

				const ids = dataResponse.data.map(({id}) => id);

				this._initialResultIds = [...this._initialResultIds, ...ids];

				this.setState(
					state => (
						{
							dataLoading: false,
							dataMap: {...state.dataMap,
								...mappedData},
							resultIds: [...state.resultIds, ...ids],
							resultIdsPinned: [...state.resultIdsPinned, ...pinnedIds],
							totalResultsVisibleCount: dataResponse.items
						}
					)
				);
			},
			2000
		);
	};

	/**
	 * Retrieves only the hidden data. This is used for showing hidden results
	 * in the hidden tab.
	 * @TODO
	 * - Swap out mock data
	 */
	_handleFetchResultsDataHidden = () => {
		const dataResponse = getMockResultsData(
			10,
			this._initialResultIdsHidden.length,
			400,
			this.state.searchBarTerm,
			{
				hidden: true
			}
		);

		const mappedData = resultsDataToMap(dataResponse.data);

		const ids = dataResponse.data.map(({id}) => id);

		this._initialResultIdsHidden = [
			...this._initialResultIdsHidden,
			...ids
		];

		this.setState(
			state => (
				{
					dataMap: {...state.dataMap,
						...mappedData},
					resultIdsHidden: [...state.resultIdsHidden, ...ids],
					totalResultsHiddenCount: dataResponse.items
				}
			)
		);
	};

	/**
	 * Handles reordering an item in a list. This will update the results list.
	 * @param {number} fromIndex The index of the item that is being moved.
	 * @param {number} toIndex The new index that the item will be moved to.
	 */
	_handleMove = (fromIndex, toIndex) => {
		this.setState(
			state => (
				{
					resultIdsPinned: move(
						state.resultIdsPinned,
						fromIndex,
						toIndex
					)
				}
			)
		);
	};

	/**
	 * Handles adding to the alias list and filters out duplicate words.
	 * @param {array} value The value of the new aliases (array of String).
	 */
	_handleUpdateAlias = value => {
		this.setState(
			state => (
				{
					aliases: [
						...state.aliases,
						...value.filter(item => !state.aliases.includes(item))
					]
				}
			)
		);
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
			addedResultsDataList
				.filter(result => !this._initialResultIds.includes(result.id))
				.map(({id}) => id),
			{
				addedResult: true,
				pinned: true
			}
		);

		this.setState(
			state => (
				{
					dataMap: {...state.dataMap,
						...newMappedData},
					resultIdsPinned: [
						...addedResultsDataList
							.filter(
								result => !state.resultIdsPinned.includes(result.id)
							)
							.map(({id}) => id),
						...state.resultIdsPinned
					]
				}
			)
		);
	};

	/**
	 * Handles updating the term in the search bar, which gets applied for
	 * fetching data.
	 * @param {string} searchBarTerm The new term
	 */
	_handleUpdateSearchBarTerm = searchBarTerm => {
		this.setState({searchBarTerm});
	};

	/**
	 * Handles removing an alias.
	 * @param {String} label Removes the alias with given label.
	 */
	_handleRemoveAlias = label => {
		this.setState(
			state => (
				{
					aliases: state.aliases.filter(item => item !== label)
				}
			)
		);
	};

	/**
	 * Handles the search bar enter, in which results are cleared and replaced
	 * with fetched data with the new search parameter.
	 */
	_handleSearchBarEnter = () => {
		this._clearResultsData();

		this._handleFetchResultsData();
		this._handleFetchResultsDataHidden();
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

	_renderHiddenInputs = () => (
		<React.Fragment>
			<input
				id="aliases"
				name="aliases"
				type="hidden"
				value={this.state.aliases}
			/>

			<input
				id="hiddenAdded"
				name="hiddenAdded"
				type="hidden"
				value={this._getHiddenAdded()}
			/>

			<input
				id="hiddenRemoved"
				name="hiddenRemoved"
				type="hidden"
				value={this._getHiddenRemoved()}
			/>

			<input
				id="pinnedAdded"
				name="pinnedAdded"
				type="hidden"
				value={this._getPinnedAdded()}
			/>

			<input
				id="pinnedRemoved"
				name="pinnedRemoved"
				type="hidden"
				value={this._getPinnedRemoved()}
			/>
		</React.Fragment>
	);

	render() {
		const {cancelUrl, searchTerm} = this.props;

		const {
			aliases,
			dataLoading,
			dataMap,
			resultIdsHidden,
			searchBarTerm,
			selected,
			totalResultsHiddenCount,
			totalResultsVisibleCount
		} = this.state;

		return (
			<div className="results-ranking-form-root">
				{this._renderHiddenInputs()}

				<PageToolbar
					onCancel={cancelUrl}
					submitDisabled={this._getDisablePublish()}
				/>

				<div className="container-fluid container-fluid-max-xl container-form-lg">
					<div className="sheet sheet-lg form-section-header">
						<h2 className="sheet-title">{`"${searchTerm}"`}</h2>

						<Alias
							keywords={aliases}
							onClickDelete={this._handleRemoveAlias}
							onClickSubmit={this._handleUpdateAlias}
							searchTerm={searchTerm}
						/>
					</div>

					<div className="sheet sheet-lg form-section-body">
						<div className="sheet-text">
							<strong>{Liferay.Language.get('results')}</strong>
						</div>

						<div className="form-section-results-list">
							<ClayTabs onSelect={this._handleTabSelect}>
								<ClayTabList className="results-ranking-tabs">
									<ClayTab>{Liferay.Language.get('visible')}</ClayTab>

									<ClayTab>{Liferay.Language.get('hidden')}</ClayTab>
								</ClayTabList>

								<ClayTabPanel>
									<List
										dataLoading={dataLoading}
										dataMap={dataMap}
										onAddResultSubmit={
											this._handleUpdateAddResultIds
										}
										onClickHide={this._handleClickHide}
										onClickPin={this._handleClickPin}
										onLoadResults={this._handleFetchResultsData}
										onMove={this._handleMove}
										onSearchBarEnter={
											this._handleSearchBarEnter
										}
										onUpdateSearchBarTerm={
											this._handleUpdateSearchBarTerm
										}
										resultIds={this._getResultIdsVisible()}
										searchBarTerm={searchBarTerm}
										selected={selected}
										totalResultsCount={
											totalResultsVisibleCount
										}
									/>
								</ClayTabPanel>

								<ClayTabPanel>
									<List
										dataLoading={dataLoading}
										dataMap={dataMap}
										onClickHide={this._handleClickHide}
										onLoadResults={
											this._handleFetchResultsDataHidden
										}
										onSearchBarEnter={
											this._handleSearchBarEnter
										}
										onUpdateSearchBarTerm={
											this._handleUpdateSearchBarTerm
										}
										resultIds={resultIdsHidden}
										searchBarTerm={searchBarTerm}
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