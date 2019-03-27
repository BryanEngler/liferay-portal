export function getMockResultsData(
	amount = 1,
	startId = 0,
	level = 100,
	properties = {}
) {
	let mockData = [];

	const PINNED_AMOUNT = 5;

	for (let i = 0; i < amount; i++) {
		const randomNum = Math.random() * 100;

		const randomNum2 = Math.floor(Math.random() * 5);

		const typeOfItem = randomNum2 < 3 ? 'Document' : 'Web Content';

		mockData.push({
			author: 'Juan Hidalgo',
			clicks: Math.floor(randomNum),
			date: 'Apr 18 2018, 11:04 AM',
			extension:
				randomNum2 < 3 ? ['doc', 'png', 'pdf'][randomNum2] : null,
			description:
				'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod',
			hidden: false,
			id: startId + i + level,
			pinned: startId + i < PINNED_AMOUNT,
			title: `${startId + i + level} This is a ${typeOfItem} Example`,
			type: typeOfItem,
			...properties
		});
	}

	return {
		items: 50,
		data: mockData
	};
}
