import React, {Component} from 'react';
import ClayButton from '../ClayButton.es';
import getCN from 'classnames';
import {getLang, sub} from 'utils/language.es';
import Item from './Item.es';
import {getMockResultsData} from 'mock-data.js';
import PaginationBar from './PaginationBar.es';
import {PropTypes} from 'prop-types';
import ReactModal from 'react-modal';
import {toggleListItem} from '../../utils/util.es';
import {resultsDataToMap} from 'utils/util.es';

const DELTAS = [5, 10, 20, 40, 50];

class AddResult extends Component {
	static propTypes = {
		onAddResultSubmit: PropTypes.func
	};

	selectAllCheckbox = React.createRef();

	state = {
		addResultSearchTerm: '',
		addResultSelectedIds: [],
		dataLoading: false,
		dataMap: {},
		page: 1,
		results: {},
		selectedDelta: 10,
		showModal: false
	};

	_clearResultSearch = () => {
		this.setState({
			addResultSearchTerm: '',
			results: {}
		});
	};

	_clearResultSelectedIds = () => {
		this.setState({addResultSelectedIds: []});
	};

	/**
	 * Sets the indeterminate state of the select all checkbox.
	 */
	componentDidUpdate() {
		const {addResultSelectedIds, results} = this.state;

		if (this.selectAllCheckbox.current) {
			const currentResultSelectedIds = this._getCurrentResultSelectedIds();

			this.selectAllCheckbox.current.indeterminate =
				addResultSelectedIds.length > 0 &&
				currentResultSelectedIds.length !== results.data.length;
		}
	}

	_fetchSearchResults = () => {
		this.setState({dataLoading: true});

		setTimeout(() => {
			this.setState(state => ({
				dataLoading: false,
				dataMap: {
					...state.dataMap,
					...resultsDataToMap(
						getMockResultsData(
							state.selectedDelta,
							state.page * state.selectedDelta -
								state.selectedDelta +
								1,
							500
						).data
					)
				},
				results: getMockResultsData(
					state.selectedDelta,
					state.page * state.selectedDelta - state.selectedDelta + 1,
					500
				)
			}));
		}, 1000);
	};

	_getCurrentResultSelectedIds = () => {
		const currentResultIds = this.state.results.data.map(
			result => result.id
		);

		return this.state.addResultSelectedIds.filter(resultId =>
			currentResultIds.includes(resultId)
		);
	};

	_handleAddResult = () => {
		this._clearResultSearch();
		this._clearResultSelectedIds();
		this._handleOpenModal();
	};

	_handleAllCheckbox = () => {
		this._getCurrentResultSelectedIds().length > 0
			? this._handleDeselectAll()
			: this._handleSelectAll();
	};

	_handleCloseModal = () => {
		this.setState({showModal: false});
	};

	_handleDeltaChange = item => {
		this.setState(state => ({
			selectedDelta: item,
			page: Math.ceil(
				(state.page * state.selectedDelta - state.selectedDelta + 1) /
					item
			)
		}));

		this._fetchSearchResults();
	};

	_handleDeselectAll = () => {
		const currentResultIds = this.state.results.data.map(
			result => result.id
		);

		this.setState(state => ({
			addResultSelectedIds: state.addResultSelectedIds.filter(
				resultId => !currentResultIds.includes(resultId)
			)
		}));
	};

	_handleOpenModal = () => {
		this.setState({
			showModal: true
		});
	};

	_handlePageChange = item => {
		this.setState({
			page: item
		});

		this._fetchSearchResults();
	};

	_handleSearchChange = event => {
		event.preventDefault();

		this.setState({
			addResultSearchTerm: event.target.value
		});
	};

	_handleSearchKeyDown = event => {
		if (event.key === 'Enter') {
			this._clearResultSelectedIds();
			this._handlePageChange(1);
		}
	};

	_handleSelect = id => {
		this.setState(state => ({
			addResultSelectedIds: toggleListItem(state.addResultSelectedIds, id)
		}));
	};

	_handleSelectAll = () => {
		this._handleDeselectAll();

		this.setState(state => ({
			addResultSelectedIds: [
				...state.addResultSelectedIds,
				...state.results.data.map(result => result.id)
			]
		}));
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
			page,
			results,
			selectedDelta,
			showModal
		} = this.state;

		const start = page * selectedDelta;

		const classManagementBar = getCN(
			'management-bar',
			addResultSelectedIds.length > 0
				? 'management-bar-primary'
				: 'management-bar-light',
			'navbar',
			'navbar-expand-md'
		);

		return (
			<li className="nav-item">
				<ClayButton
					displayStyle="primary"
					label={getLang('add-a-result')}
					key="ADD_RESULT_BUTTON"
					onClick={this._handleAddResult}
				/>

				<ReactModal
					isOpen={showModal}
					contentLabel="addResultModal"
					onRequestClose={this._handleCloseModal}
					className="modal-dialog modal-lg modal-full-screen-sm-down results-ranking-add-result-modal"
					overlayClassName="modal-backdrop react-modal-backdrop"
				>
					<div className="modal-content">
						<div className="modal-header">
							<div className="modal-title">
								{getLang('add-a-result')}
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
													onChange={
														this._handleSearchChange
													}
													onKeyDown={
														this
															._handleSearchKeyDown
													}
													placeholder={getLang(
														'search-your-engine'
													)}
													type="text"
													value={addResultSearchTerm}
												/>

												<div className="input-group-inset-item input-group-inset-item-after">
													<ClayButton
														displayStyle={
															'unstyled'
														}
														iconName="search"
														onClick={
															this
																._fetchSearchResults
														}
													/>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						{results.items && results.data ? (
							<div className="modal-body inline-scroller">
								{dataLoading ? (
									<div className="list-group sheet">
										<div className="sheet-title">
											<div className="load-more-container">
												<span className="loading-animation" />
											</div>
										</div>
									</div>
								) : (
									[
										<div
											className={classManagementBar}
											key={0}
										>
											<div className="container-fluid container-fluid-max-xl">
												<ul className="navbar-nav navbar-nav-expand">
													<li className="nav-item">
														<div className="custom-control custom-checkbox">
															<label>
																<input
																	aria-label="Checkbox for search results"
																	checked={
																		this._getCurrentResultSelectedIds()
																			.length ===
																		results
																			.data
																			.length
																	}
																	className="custom-control-input"
																	onChange={
																		this
																			._handleAllCheckbox
																	}
																	ref={
																		this
																			.selectAllCheckbox
																	}
																	type="checkbox"
																/>

																<span className="custom-control-label" />
															</label>
														</div>
													</li>

													<li className="nav-item">
														<span className="navbar-text">
															{addResultSelectedIds.length >
															0
																? sub(
																		getLang(
																			'x-items-selected'
																		),
																		[
																			addResultSelectedIds.length
																		]
																  )
																: sub(
																		getLang(
																			'x-to-x-of-x-results'
																		),
																		[
																			start -
																				selectedDelta +
																				1,
																			Math.min(
																				start,
																				results.items
																			),
																			results.items
																		]
																  )}
														</span>
													</li>

													{addResultSelectedIds.length >
														0 && (
														<li className="nav-item nav-item-shrink">
															<ClayButton
																borderless
																label={getLang(
																	'clear-all-selected'
																)}
																onClick={
																	this
																		._clearResultSelectedIds
																}
																size="sm"
															/>
														</li>
													)}
												</ul>
											</div>
										</div>,

										<ul className="list-group" key={1}>
											{results.data.map(result => (
												<Item
													author={result.author}
													clicks={result.clicks}
													date={result.date}
													extension={result.extension}
													hidden={result.hidden}
													id={result.id}
													key={result.id}
													onSelect={
														this._handleSelect
													}
													selected={addResultSelectedIds.includes(
														result.id
													)}
													title={result.title}
													type={result.type}
												/>
											))}
										</ul>
									]
								)}

								<PaginationBar
									deltas={DELTAS}
									page={page}
									selectedDelta={selectedDelta}
									onDeltaChange={this._handleDeltaChange}
									onPageChange={this._handlePageChange}
									totalItems={results.items}
								/>
							</div>
						) : (
							<div className="modal-body inline-scroller">
								<div className="sheet">
									<div className="sheet-text text-center">
										{dataLoading ? (
											<div className="load-more-container">
												<span className="loading-animation" />
											</div>
										) : (
											getLang(
												'search-your-engine-to-display-results'
											)
										)}
									</div>
								</div>
							</div>
						)}

						<div className="modal-footer">
							<div className="modal-item-last">
								<div className="btn-group">
									<div className="btn-group-item">
										<ClayButton
											borderless
											label={getLang('cancel')}
											onClick={this._handleCloseModal}
										/>
									</div>

									<div className="btn-group-item">
										<ClayButton
											disabled={
												addResultSelectedIds.length ===
												0
											}
											displayStyle="primary"
											label={getLang('add')}
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
