import React, {Component} from 'react';
import {PropTypes} from 'prop-types';
import CreatableSelect from 'react-select/lib/Creatable';

const components = {
	DropdownIndicator: null
};

const createOption = label => ({
	label,
	value: label
});

class ReactSelectTags extends Component {
	static propTypes = {
		value: PropTypes.arrayOf(String),
		onAction: PropTypes.func
	};

	state = {
		inputValue: '',
		value: this.props.value
	};

	handleChange = (value, actionMeta) => {
		this.props.onAction(value);
	};

	handleInputChange = inputValue => {
		this.setState({inputValue});
	};

	handleKeyDown = event => {
		const {value} = this.props;
		const {inputValue} = this.state;

		if (!inputValue) return;
		switch (event.key) {
			case 'Enter':
			case 'Tab':
			case ',':
				this.props.onAction([...value, createOption(inputValue)]);
				this.setState({
					inputValue: ''
				});
				event.preventDefault();
				break;
			default:
		}
	};

	render() {
		const {value} = this.props;
		const {inputValue} = this.state;

		return (
			<CreatableSelect
				className="react-select-container"
				classNamePrefix="react-select"
				components={components}
				inputValue={inputValue}
				isClearable
				isMulti
				menuIsOpen={false}
				onChange={this.handleChange}
				onInputChange={this.handleInputChange}
				onKeyDown={this.handleKeyDown}
				placeholder=""
				value={value}
			/>
		);
	}
}

export default ReactSelectTags;
