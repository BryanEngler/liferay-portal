import 'clay-button';
import 'metal';
import 'metal-component';
import {Config} from 'metal-state';
import Soy from 'metal-soy';
import templates from './ResultRankings.soy';

class ResultRankings {

	_addNewRanking() {
		/* TODO: Redirect to new ranking form */
	}
}

ResultRankings.STATE = { };

Soy.register(ResultRankings, templates);

export {ResultRankings};
export default ResultRankings;