import ClayButton from './ClayButton.es';
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import ReactModal from 'react-modal';
import ReactSelectTags from './ReactSelectTags.es';
import Tag from './Tag.es';
import {getLang} from 'utils/language.es';

class Alias extends Component {
	static propTypes = {
		keywords: PropTypes.arrayOf(String),
		onClickDelete: PropTypes.func,
		onClickSubmit: PropTypes.func,
		searchTerm: PropTypes.string
	};

	state = {
		modalKeywords: [],
		showModal: false
	};

	_handleCloseModal = () => {
		this.setState({showModal: false});
	};

	_handleOpenModal = () => {
		this.setState(
			{
				modalKeywords: [],
				showModal: true
			}
		);
	};

	_handleSubmit = () => {
		this.props.onClickSubmit(
			this.state.modalKeywords.map(item => item.value)
		);

		this._handleCloseModal();
	};

	_handleUpdate = value => {
		this.setState({modalKeywords: value});
	};

	render() {
		const {keywords, onClickDelete, searchTerm} = this.props;

		const {modalKeywords} = this.state;

		return (
			<div className="results-ranking-alias">
				<div className="sheet sheet-lg">
					<h2 className="sheet-title">{`"${searchTerm}"`}</h2>

					<div className="sheet-text">
						<div className="alias-title">
							<strong>{getLang('aliases')}</strong>
						</div>

						<div className="input-group">
							<div className="input-group-item input-group-item-shrink">
								{keywords.map(
									word => (
										<Tag
											key={word}
											label={word}
											onClickDelete={onClickDelete}
										/>
									)
								)}
							</div>

							<div className="input-group-item input-group-item-shrink">
								<ClayButton
									borderless
									displayStyle="primary"
									onClick={this._handleOpenModal}
									label={getLang('add-an-alias')}
									size="sm"
								/>
							</div>
						</div>
					</div>
				</div>

				<ReactModal
					className="modal-dialog modal-lg results-ranking-alias-modal"
					contentLabel="aliasModal"
					isOpen={this.state.showModal}
					onRequestClose={this._handleCloseModal}
					overlayClassName="modal-backdrop react-modal-backdrop"
				>
					<div className="modal-content">
						<div className="modal-header">
							<div className="modal-title">
								{getLang('add-an-alias')}
							</div>

							<ClayButton
								borderless
								iconName="times"
								onClick={this._handleCloseModal}
							/>
						</div>

						<div className="modal-body">
							<div className="alias-modal-description">
								{getLang('add-an-alias-description')}
							</div>

							<div className="form-group">
								<label>{getLang('alias')}</label>

								<ReactSelectTags
									onAction={this._handleUpdate}
									value={modalKeywords}
								/>

								<div className="form-feedback-group">
									<div className="form-text">
										{getLang('add-an-alias-instruction')}
									</div>
								</div>
							</div>
						</div>

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
												modalKeywords.length === 0
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
			</div>
		);
	}
}

export default Alias;