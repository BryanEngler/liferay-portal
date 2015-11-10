package com.liferay.demo.geolocation;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GeolocationDemoDatasetBulkLoaderMainAsArquillianTest {

	@Test
	public void exampleSearch() throws Exception {
		new GeolocationDemoSearch().search();
	}

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule testRule =
		new LiferayIntegrationTestRule();

}
