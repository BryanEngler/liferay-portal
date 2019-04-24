import ClayButton from 'components/shared/ClayButton.es';
import PropTypes from 'prop-types';
import React, {Component} from 'react';

class PageToolbar extends Component {
	static props = {
		onCancel: PropTypes.string,
		onPublish: PropTypes.func,
		onSaveAsDraft: PropTypes.func,
		submitDisabled: PropTypes.bool
	};

	static defaultProps = {
		submitDisabled: true
	};

	render() {
		const {
			onCancel,
			onPublish,
			onSaveAsDraft,
			submitDisabled
		} = this.props;

		return (
			<nav className="page-toolbar-root tbar upper-tbar">
				<div className="container-fluid container-fluid-max-xl">
					<ul className="tbar-nav">
						<li className="tbar-item tbar-item-expand" />

						<li className="tbar-item">
							<ClayButton
								borderless
								href={onCancel}
								label={Liferay.Language.get('cancel')}
								size="sm"
							/>
						</li>

						<li className="tbar-item">
							<ClayButton
								label={Liferay.Language.get('save-as-draft')}
								onClick={onSaveAsDraft}
								size="sm"
							/>
						</li>

						<li className="tbar-item">
							<ClayButton
								disabled={submitDisabled}
								displayStyle="primary"
								label={Liferay.Language.get('publish')}
								onClick={onPublish}
								size="sm"
								type="submit"
							/>
						</li>
					</ul>
				</div>
			</nav>
		);
	}
}

export default PageToolbar;