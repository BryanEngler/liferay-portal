import Uri from 'metal-uri';

/**
 * Fetches documents and maps the data response to the expected object shape
 * of {items: [{}, {}, ...], total: 10}.
 * @param {string} url The base url to fetch documents.
 * @param {Object} params The url parameters to be included in the request.
 * @returns {Promise} The fetch request promise.
 */
export function fetchDocuments(url, params) {
	const fetchUri = new Uri(url);

	for (const property in params) {
		if (params[property]) {
			fetchUri.setParameterValue(property, params[property]);
		}
	}

	return fetch(fetchUri)
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