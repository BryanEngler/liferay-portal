package com.liferay.portal.search.document;

public enum FieldType {

	/*
	fields that dont have an OOTB mapping should use the CUSTOM type. ie, its a
	field that a customer contributes and defines on their own if they have some
	unique data type they have created
	 */
	CUSTOM(""),
	LONG(        //enum name matches the dynamic template "name"
		"_long"  //value matches the dynamic template "match" pattern/suffix
	),
	LONG_WITH_KEYWORD_MULTIFIELD("_long_with_keyword_multifield"),
	LONG_WITH_TEXT_MULTIFIELD("_long_2"); //the match pattern doesnt necessarily need to match the template name

	public String getMatchPatten() {
		return _matchPatten;
	}

	private FieldType(String matchPatten) {
		_matchPatten = matchPatten;
	}

	private final String _matchPatten;

}
