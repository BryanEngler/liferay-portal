// @TODO Replace with real endpoint. i.e. /o/headless-search/v1.0/search

const DOCUMENT_API_BASE_URL = 'http://www.mocky.io/v2/5cabd1073000002900103260';

/**
 * Fetches documents.
 * @param {number} config.companyId
 * @param {number} config.end
 * @param {boolean} config.hidden
 * @param {string} config.searchIndex
 * @param {string} config.keywords
 * @param {number} config.start
 */
export function fetchDocuments(config) {
	const {companyId, end, hidden, keywords, searchIndex, start} = config;

	let url = `${DOCUMENT_API_BASE_URL}
		/${searchIndex}
		/${keywords}
		/${hidden}
		/${companyId}
		/${start}
		/${end}`;

	// @TODO Remove. This is for getting mocked hidden results.

	if (hidden) {
		url = 'http://www.mocky.io/v2/5cabd9ab3000002900103266';
	}

	return fetch(url)
		.then(response => response.json())
		.then(
			data => (
				{
					items: data.documents,
					total: data.total
				}
			)
		);
}