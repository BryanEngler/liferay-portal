export function getMockResultsData(
	amount = 1,
	startId = 0,
	level = 100,
	searchBarTerm = '',
	properties = {}
) {
	const mockData = [];

	const PINNED_AMOUNT = 5;

	for (let i = 0; i < amount; i++) {
		const randomNum = Math.random() * 100;

		const randomNum2 = Math.floor(Math.random() * 5);

		const typeOfItem = randomNum2 < 3 ? 'Document' : 'Web Content';

		const k = searchBarTerm === '' ? i + startId : (i + startId) * 2;

		mockData.push(
			{
				author: 'Juan Hidalgo',
				clicks: Math.floor(randomNum),
				date: 'Apr 18 2018, 11:04 AM',
				description:
				'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod',
				extension:
					randomNum2 < 3 ? ['doc', 'png', 'pdf'][randomNum2] : null,
				hidden: false,
				id: k + level,
				pinned: k < PINNED_AMOUNT,
				title: `${k + level} This is a ${typeOfItem} Example`,
				type: typeOfItem,
				...properties
			}
		);
	}

	return {
		data: mockData,
		items: 50
	};
}