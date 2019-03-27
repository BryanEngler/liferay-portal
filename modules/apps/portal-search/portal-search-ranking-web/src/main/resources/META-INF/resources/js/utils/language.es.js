const SPLIT_REGEX = /({\d+})/g;

/**
 * Liferay function to get the language key string.
 * @param {string} key Language key string to retrieve.
 */
// export const getLang = key => Liferay.Language.get(key);

/**
 * @TODO Replace this function with the getLang function above when moving this
 * inside portal code.
 */
export const getLang = key => {
	const KEY_MAP = {
		add: 'Add',
		'add-a-result': 'Add a Result',
		'add-an-alias': 'Add an Alias',
		'add-an-alias-description':
			'A short description to explain how aliases work',
		'add-an-alias-instruction':
			'Type a comma or press enter to input an alias',
		alias: 'Alias',
		aliases: 'Aliases',
		cancel: 'Cancel',
		'clear-all-selected': 'Clear All Selected',
		'clicks-x': 'Clicks {0}',
		'contains-text': 'Contains text',
		'customize-results-for-x': 'Customize Results for "{0}"',
		hidden: 'Hidden',
		'hide-result': 'Hide result',
		'hide-results': 'Hide results',
		'items-selected': 'items selected',
		'load-more-results': 'Load More Results',
		modified: 'Modified',
		'pin-result': 'Pin result',
		'pin-results': 'Pin results',
		publish: 'Publish',
		results: 'Results',
		'save-as-draft': 'Save as Draft',
		'search-your-engine': 'Search your engine',
		'search-your-engine-to-display-results':
			'Search your engine to display results',
		'show-result': 'Show result',
		'show-results': 'Show results',
		'showing-x-to-x-of-x-entries': 'Showing {0} to {1} of {2} entries.',
		synonyms: 'Synonyms',
		tag: 'Tag',
		type: 'Type',
		'unpin-result': 'Unpin result',
		'unpin-results': 'Unpin results',
		user: 'User',
		visible: 'Visible',
		'x-items': '{0} items',
		'x-items-selected': '{0} items selected',
		'x-of-x-items-selected': '{0} of {1} items selected',
		'x-to-x-of-x-results': '{0}-{1} of {2} results'
	};

	return KEY_MAP[key] || key;
};

/**
 * Utility function for substituting variables into language keys.
 *
 * Examples:
 * sub(Liferay.Language.get('search-x'), ['all'])
 * => 'search all'
 * sub(Liferay.Language.get('search-x'), [<b>all<b>], false)
 * => 'search <b>all</b>'
 *
 * @param {string} langKey This is the language key used from our properties file
 * @param {string} args Arguments to pass into language key
 * @param {string} join Boolean used to indicate whether to call `.join()` on
 * the array before it is returned. Use `false` if subbing in JSX.
 * @return {(string|Array)}
 */
export function sub(langKey, args, join = true) {
	const keyArray = langKey.split(SPLIT_REGEX).filter(val => val.length !== 0);

	for (let i = 0; i < args.length; i++) {
		const arg = args[i];

		const indexKey = `{${i}}`;

		let argIndex = keyArray.indexOf(indexKey);

		while (argIndex >= 0) {
			keyArray.splice(argIndex, 1, arg);

			argIndex = keyArray.indexOf(indexKey);
		}
	}

	return join ? keyArray.join('') : keyArray;
}
