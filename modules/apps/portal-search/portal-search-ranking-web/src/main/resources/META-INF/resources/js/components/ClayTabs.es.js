import React from 'react';
import getCN from 'classnames';
import {Tab, Tabs, TabList, TabPanel} from 'react-tabs';

const ClayTab = ({children, selected, selectedClassName, ...otherProps}) => {
	const classes = getCN('btn', 'btn-unstyled', 'nav-link', {
		[selectedClassName]: selected
	});

	return (
		<Tab className="nav-item" selected={selected} {...otherProps}>
			<button className={classes}>{children}</button>
		</Tab>
	);
};

ClayTab.tabsRole = 'Tab';

const ClayTabs = ({children, ...otherProps}) => (
	<Tabs selectedTabClassName="active" {...otherProps}>
		{children}
	</Tabs>
);

const ClayTabList = ({children, className, ...otherProps}) => {
	const classes = getCN('nav', 'nav-underline', className);

	return (
		<TabList className={classes} {...otherProps}>
			{children}
		</TabList>
	);
};

ClayTabList.tabsRole = 'TabList';

export {ClayTabs, ClayTab, ClayTabList, TabPanel as ClayTabPanel};
