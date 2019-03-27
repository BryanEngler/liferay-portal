import {
	move,
	resultsDataToMap,
	toggleListItem,
	updateDataMap
} from 'utils/util.es';

const RESULTS_LIST = [
	{
		author: 'Juan Hidalgo',
		clicks: 289,
		date: 'Apr 18 2018, 11:04 AM',
		description:
			'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod',
		id: 102,
		title: 'This is a Web Content Example with Long Title',
		type: 'Web Content'
	},
	{
		author: 'Juan Hidalgo',
		clicks: 8,
		date: 'Apr 18 2018, 11:04 AM',
		extension: 'png',
		id: 103,
		title: 'This is an Image Example',
		type: 'Document'
	}
];

const TEST_LIST = [{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}];

describe('utils', () => {
	describe('move', () => {
		it('should return an array with an item moved downward', () => {
			expect(move(TEST_LIST, 0, 2)).toEqual([
				{id: 2},
				{id: 1},
				{id: 3},
				{id: 4},
				{id: 5}
			]);

			expect(move(TEST_LIST, 1, 4)).toEqual([
				{id: 1},
				{id: 3},
				{id: 4},
				{id: 2},
				{id: 5}
			]);
		});

		it('should return an array with an item moved upward', () => {
			expect(move(TEST_LIST, 2, 0)).toEqual([
				{id: 3},
				{id: 1},
				{id: 2},
				{id: 4},
				{id: 5}
			]);

			expect(move(TEST_LIST, 4, 2)).toEqual([
				{id: 1},
				{id: 2},
				{id: 5},
				{id: 3},
				{id: 4}
			]);
		});

		it('should move items in a string list', () => {
			expect(move(['one', 'two', 'three'], 0, 2)).toEqual([
				'two',
				'one',
				'three'
			]);
		});

		it('should move items in a number list', () => {
			expect(move([1, 2, 3], 0, 2)).toEqual([2, 1, 3]);
		});

		it('should move an item to the end of the to index exceeds the list length', () => {
			expect(move(TEST_LIST, 0, 99)).toEqual([
				{id: 2},
				{id: 3},
				{id: 4},
				{id: 5},
				{id: 1}
			]);

			expect(move(TEST_LIST, 3, 10)).toEqual([
				{id: 1},
				{id: 2},
				{id: 3},
				{id: 5},
				{id: 4}
			]);
		});
	});

	describe('resultsDataToMap', () => {
		it('should return a mapped set of data', () => {
			expect(resultsDataToMap(RESULTS_LIST)).toEqual({
				102: RESULTS_LIST.filter(({id}) => id === 102)[0],
				103: RESULTS_LIST.filter(({id}) => id === 103)[0]
			});
		});
	});

	describe('toggleListItem', () => {
		it('should return a new list', () => {
			expect(toggleListItem([102, 103, 104], 102)).toEqual([103, 104]);
			expect(toggleListItem([102, 103, 104], 103)).toEqual([102, 104]);
			expect(toggleListItem([102, 103, 104], 105)).toEqual([
				102,
				103,
				104,
				105
			]);
		});
	});

	describe('updateDataMap', () => {
		it('should a data map object with the updated properties', () => {
			const initialDataMap = {
				101: {
					id: 101,
					pinned: false
				},
				102: {
					id: 102,
					pinned: false
				}
			};

			const updatedDataMap = {
				101: {
					id: 101,
					pinned: false
				},
				102: {
					id: 102,
					pinned: true
				}
			};

			expect(
				updateDataMap(initialDataMap, [102], {pinned: true})
			).toEqual(updatedDataMap);
		});

		it('should multiple items from a data map object', () => {
			const initialDataMap = {
				101: {
					id: 101,
					pinned: false
				},
				102: {
					id: 102,
					pinned: false
				}
			};

			const updatedDataMap = {
				101: {
					id: 101,
					pinned: true
				},
				102: {
					id: 102,
					pinned: true
				}
			};

			expect(
				updateDataMap(initialDataMap, [101, 102], {pinned: true})
			).toEqual(updatedDataMap);
		});

		it('should a data map object with the multiple updated properties', () => {
			const initialDataMap = {
				101: {
					id: 101,
					pinned: false,
					hidden: false
				},
				102: {
					id: 102,
					pinned: false,
					hidden: false
				}
			};

			const updatedDataMap = {
				101: {
					id: 101,
					hidden: false,
					pinned: false
				},
				102: {
					id: 102,
					hidden: true,
					pinned: true
				}
			};

			expect(
				updateDataMap(initialDataMap, [102], {
					hidden: true,
					pinned: true
				})
			).toEqual(updatedDataMap);
		});
	});
});
