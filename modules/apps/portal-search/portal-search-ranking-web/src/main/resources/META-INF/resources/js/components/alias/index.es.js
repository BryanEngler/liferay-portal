import ClayButton from 'components/shared/ClayButton.es';
import ClayMultiselect from 'components/shared/ClayMultiselect.es';
import PropTypes from 'prop-types';
import React, {Component} from 'react';
import ReactModal from 'react-modal';
import Tag from './Tag.es';

class Alias extends Component {
	static propTypes = {
		keywords: PropTypes.arrayOf(String),
		onClickDelete: PropTypes.func.isRequired,
		onClickSubmit: PropTypes.func.isRequired
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
		const {keywords, onClickDelete} = this.props;

		const {modalKeywords} = this.state;

		return (
			<div className="results-ranking-alias-root">
				<div className="sheet-text">
					<div className="alias-title">
						<strong>{Liferay.Language.get('aliases')}</strong>
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
								label={Liferay.Language.get('add-an-alias')}
								onClick={this._handleOpenModal}
								size="sm"
							/>
						</div>
					</div>
				</div>

				<ReactModal
					className="modal-dialog modal-dialog-lg alias-modal-root"
					contentLabel="aliasModal"
					isOpen={this.state.showModal}
					onRequestClose={this._handleCloseModal}
					overlayClassName="modal-backdrop react-modal-backdrop"
					portalClassName="results-ranking-modal-root"
				>
					<div className="modal-content">
						<div className="modal-header">
							<div className="modal-title">
								{Liferay.Language.get('add-an-alias')}
							</div>

							<ClayButton
								borderless
								iconName="times"
								onClick={this._handleCloseModal}
							/>
						</div>

						<div className="modal-body">
							<div className="alias-modal-description">
								{Liferay.Language.get('add-an-alias-description')}
							</div>

							<div className="form-group">
								<label>{Liferay.Language.get('alias')}</label>

								<ClayMultiselect
									onAction={this._handleUpdate}
									value={modalKeywords}
								/>

								<div className="form-feedback-group">
									<div className="form-text">
										{Liferay.Language.get('add-an-alias-instruction')}
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
											label={Liferay.Language.get('cancel')}
											onClick={this._handleCloseModal}
										/>
									</div>

									<div className="btn-group-item">
										<ClayButton
											disabled={
												modalKeywords.length === 0
											}
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
			</div>
		);
	}
}

export default Alias;