import ClayButton from 'components/shared/ClayButton.es';
import ClayEmptyState from 'components/shared/ClayEmptyState.es';
import getCN from 'classnames';
import Item from 'components/list/Item.es';
import PaginationBar from './PaginationBar.es';
import React, {Component} from 'react';
import ReactModal from 'react-modal';
import ThemeContext from 'ThemeContext.es';
import {fetchDocuments} from 'utils/api.es';
import {PropTypes} from 'prop-types';
import {resultsDataToMap} from 'utils/util.es';
import {sub} from 'utils/language.es';
import {toggleListItem} from '../../utils/util.es';

const DELTAS = [5, 10, 20, 40, 50];

class AddResult extends Component {
	static contextType = ThemeContext;

	static propTypes = {
		onAddResultSubmit: PropTypes.func
	};

	selectAllCheckbox = React.createRef();

	state = {
		addResultSearchTerm: '',
		addResultSelectedIds: [],
		dataLoading: false,
		dataMap: {},
		displayInitialMessage: true,
		page: 1,
		results: {},
		selectedDelta: 10,
		showModal: false
	};

	_clearResultSearch = () => {
		this.setState(
			{
				addResultSearchTerm: '',
				results: {}
			}
		);
	};

	_clearResultSelectedIds = () => {
		this.setState({addResultSelectedIds: []});
	};

	/**
	 * Sets the indeterminate state of the select all checkbox.
	 */
	componentDidUpdate() {
		const {addResultSelectedIds, results} = this.state;

		const checkboxElement = this.selectAllCheckbox.current;

		if (checkboxElement) {
			const currentResultSelectedIds = this._getCurrentResultSelectedIds();

			checkboxElement.indeterminate = addResultSelectedIds.length > 0 &&
				currentResultSelectedIds.length !== results.items.length;
		}
	}

	_fetchSearchResults = () => {
		const {addResultSearchTerm, page, selectedDelta} = this.state;

		this.setState({dataLoading: true});

		fetchDocuments(
			{
				companyId: this.context.companyId,
				end: (page * selectedDelta) - 1,
				hidden: false,
				keywords: addResultSearchTerm,
				searchIndex: this.context.searchIndex,
				start: (page * selectedDelta) - selectedDelta
			}
		).then(
			({items, total}) => {
				this.setState(
					state => (
						{
							dataLoading: false,
							dataMap: {
								...state.dataMap,
								...resultsDataToMap(items)
							},
							displayInitialMessage: false,
							results: {
								items,
								total
							}
						}
					)
				);
			}
		);
	};

	_getCurrentResultSelectedIds = () => {
		const {addResultSelectedIds, results} = this.state;

		const currentResultIds = results.items.map(
			result => result.id
		);

		return addResultSelectedIds.filter(
			resultId => currentResultIds.includes(resultId)
		);
	};

	_handleAddResult = () => {
		this._clearResultSearch();
		this._clearResultSelectedIds();

		this._handleOpenModal();
	};

	_handleAllCheckbox = () => {
		if (this._getCurrentResultSelectedIds().length > 0) {
			this._handleDeselectAll();
		}
		else {
			this._handleSelectAll();
		}
	};

	_handleClearAllSelected = () => {
		this._clearResultSelectedIds();
	};

	_handleCloseModal = () => {
		this.setState(
			{
				displayInitialMessage: true,
				showModal: false
			}
		);
	};

	_handleDeltaChange = item => {
		this.setState(
			state => ({
				page: Math.ceil((state.page * state.selectedDelta - state.selectedDelta + 1) / item),
				selectedDelta: item
			}),
			this._fetchSearchResults
		);
	};

	_handleDeselectAll = () => {
		const currentResultIds = this.state.results.items.map(
			result => result.id
		);

		this.setState(
			state => ({
				addResultSelectedIds: state.addResultSelectedIds.filter(
					resultId => !currentResultIds.includes(resultId)
				)
			})
		);
	};

	_handleOpenModal = () => {
		this.setState({showModal: true});
	};

	_handlePageChange = item => {
		this.setState(
			{page: item},
			this._fetchSearchResults
		);
	};

	_handleSearchChange = event => {
		event.preventDefault();

		this.setState({addResultSearchTerm: event.target.value});
	};

	_handleSearchEnter = () => {
		this._clearResultSelectedIds();

		this._handlePageChange(1);
	};

	_handleSearchKeyDown = event => {
		if (event.key === 'Enter') {
			this._handleSearchEnter();
		}
	};

	_handleSelect = id => {
		this.setState(
			state => ({
				addResultSelectedIds: toggleListItem(state.addResultSelectedIds, id)
			})
		);
	};

	_handleSelectAll = () => {
		this._handleDeselectAll();

		this.setState(
			state => ({
				addResultSelectedIds: [
					...state.addResultSelectedIds,
					...state.results.items.map(result => result.id)
				]
			})
		);
	};

	_handleSubmit = event => {
		event.preventDefault();

		const addResultDataList = this.state.addResultSelectedIds.map(
			id => this.state.dataMap[id]
		);

		this.props.onAddResultSubmit(addResultDataList);

		this._handleCloseModal();
	};

	render() {
		const {
			addResultSearchTerm,
			addResultSelectedIds,
			dataLoading,
			displayInitialMessage,
			page,
			results,
			selectedDelta,
			showModal
		} = this.state;

		const start = page * selectedDelta;

		const classManagementBar = getCN(
			'management-bar',
			addResultSelectedIds.length > 0 ?
				'management-bar-primary' :
				'management-bar-light',
			'navbar',
			'navbar-expand-md'
		);

		return (
			<li className="nav-item">
				<ClayButton
					displayStyle="primary"
					key="ADD_RESULT_BUTTON"
					label={Liferay.Language.get('add-a-result')}
					onClick={this._handleAddResult}
				/>

				<ReactModal
					className="modal-dialog modal-dialog-lg modal-full-screen-sm-down add-result-modal-root"
					contentLabel="addResultModal"
					isOpen={showModal}
					onRequestClose={this._handleCloseModal}
					overlayClassName="modal-backdrop react-modal-backdrop"
					portalClassName="results-ranking-modal-root"
				>
					<div className="modal-content">
						<div className="modal-header">
							<div className="modal-title">
								{Liferay.Language.get('add-a-result')}
							</div>

							<ClayButton
								borderless
								iconName="times"
								onClick={this._handleCloseModal}
							/>
						</div>

						<div className="modal-header">
							<div className="container-fluid container-fluid-max-xl">
								<div className="management-bar navbar-expand-md">
									<div className="navbar-form navbar-form-autofit">
										<div className="input-group">
											<div className="input-group-item">
												<input
													aria-label="Search for"
													className="form-control input-group-inset input-group-inset-after"
													onChange={this._handleSearchChange}
													onKeyDown={this._handleSearchKeyDown}
													placeholder={Liferay.Language.get(
														'search-your-engine'
													)}
													type="text"
													value={addResultSearchTerm}
												/>

												<div className="input-group-inset-item input-group-inset-item-after">
													<ClayButton
														displayStyle="unstyled"
														iconName="search"
														onClick={this._handleSearchEnter}
													/>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						<div className="modal-body inline-scroller">
							{dataLoading && (
								<div className="list-group sheet">
									<div className="sheet-title">
										<div className="load-more-container">
											<span className="loading-animation" />
										</div>
									</div>
								</div>
							)}

							{!dataLoading && (
								results.total && results.items ?
									<React.Fragment>
										<div className={classManagementBar}>
											<div className="container-fluid container-fluid-max-xl">
												<ul className="navbar-nav navbar-nav-expand">
													<li className="nav-item">
														<div className="custom-control custom-checkbox">
															<label>
																<input
																	aria-label="Checkbox for search results"
																	checked={this._getCurrentResultSelectedIds().length === results.items.length}
																	className="custom-control-input"
																	onChange={this._handleAllCheckbox}
																	ref={this.selectAllCheckbox}
																	type="checkbox"
																/>

																<span className="custom-control-label" />
															</label>
														</div>
													</li>

													<li className="nav-item">
														<span className="navbar-text">
															{addResultSelectedIds.length > 0 ?
																sub(
																	Liferay.Language.get('x-items-selected'),
																	[
																		addResultSelectedIds.length
																	]
																) :
																sub(
																	Liferay.Language.get('x-x-of-x-results'),
																	[
																		start - selectedDelta + 1,
																		Math.min(start, results.total),
																		results.total
																	]
																)
															}
														</span>
													</li>

													{addResultSelectedIds.length >
														0 && (
														<li className="nav-item nav-item-shrink">
															<ClayButton
																borderless
																label={Liferay.Language.get(
																	'clear-all-selected'
																)}
																onClick={this._handleClearAllSelected}
																size="sm"
															/>
														</li>
													)}
												</ul>
											</div>
										</div>

										<ul className="list-group">
											{results.items.map(
												result => (
													<Item
														author={result.author}
														clicks={result.clicks}
														date={result.date}
														extension={result.extension}
														hidden={result.hidden}
														id={result.id}
														key={result.id}
														onSelect={this._handleSelect}
														selected={addResultSelectedIds.includes(result.id)}
														title={result.title}
														type={result.type}
													/>
												)
											)}
										</ul>

										<PaginationBar
											deltas={DELTAS}
											onDeltaChange={this._handleDeltaChange}
											onPageChange={this._handlePageChange}
											page={page}
											selectedDelta={selectedDelta}
											totalItems={results.total}
										/>
									</React.Fragment> :
									<div className="sheet">
										{displayInitialMessage ?
											<ClayEmptyState
												description={Liferay.Language.get('search-your-engine-to-display-results')}
												displayState="empty"
												title={Liferay.Language.get('search-your-engine')}
											/> :
											<ClayEmptyState />
										}
									</div>
							)}
						</div>

						<div className="modal-footer">
							<div className="modal-item-last">
								<div className="btn-group">
									<div className="btn-group-item">
										<ClayButton
											borderless
											label={Liferay.Language.get('cancel')}
											onClick={this._handleCloseModal}
										/>
									</div>

									<div className="btn-group-item">
										<ClayButton
											disabled={addResultSelectedIds.length === 0}
											displayStyle="primary"
											label={Liferay.Language.get('add')}
											onClick={this._handleSubmit}
										/>
									</div>
								</div>
							</div>
						</div>
					</div>
				</ReactModal>
			</li>
		);
	}
}

export default AddResult;