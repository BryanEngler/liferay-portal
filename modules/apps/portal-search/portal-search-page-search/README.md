### Crawling Liferay with Apache Nutch 1.x Demo

Steps:
1. Build this branch. The modules that were changed/added are:
- modules/apps/portal-search-elasticsearch6/portal-search-elasticsearch6-impl
- modules/apps/portal-search/portal-search-admin-web
- modules/apps/portal-search/portal-search-api
- modules/apps/portal-search/portal-search-engine-adapter-api
- modules/apps/portal-search/portal-search-page-search
- modules/apps/portal-search/portal-search-web-api
- modules/apps/portal-search/portal-search-web

2. Download Apache Nutch 1.15 from [here](http://www.apache.org/dyn/closer.cgi/nutch/).

3. Extract the contents of the download to your `$LIFERAY_HOME` directory (where the tomcat, data, deploy, etc folders are)

4. To ensure the unpacked content is working, run `$LIFERAY_HOME/apache-nutch-1.15/bin/nutch`. Should get something like this

        nutch 1.15
        Usage: nutch COMMAND
        where COMMAND is one of:
        readdb            read / dump crawl db
        mergedb           merge crawldb-s, with optional filtering
        readlinkdb        read / dump link db
        inject            inject new urls into the database
        generate          generate new segments to fetch from crawl db
        ...

5. You can give a name for the cluster, by adding the lines below to `$LIFERAY_HOME/apache-nutch-1.15/conf/nutch-default.xml`:

        <property>
         <name>http.agent.name</name>
         <value>My Nutch Spider</value>
        </property>

6. Add `example.com` to `/etc/hosts`, pointing to `localhost`:

        127.0.0.1   example.com

7. Let’s only accpet URLs in `example.com`, so in `$LIFERAY_HOME/apache-nutch-1.15/conf/regex-urlfilter.xml` we replace

        +.

    with

        +^http://example.com:8080/

8. Liferay uses a lot of query strings, but the defaut Nutch setup filter them out. We change this configuration in `$LIFERAY_HOME/apache-nutch-1.15/conf/regex-urlfilter.xml`, commenting out the line:

        #-[?*!@=]

9. Create some seed URLs. Those are the URLs to be first retrieved.

        mkdir -p $LIFERAY_HOME/apache-nutch-1.15/urls
        echo 'http://example.com:8080/one' > $LIFERAY_HOME/apache-nutch-1.15/urls/seed.txt

10. Configure nutch's connection to elasticsearch. In `$LIFERAY_HOME/apache-nutch-1.15/conf/index-writers.xml`, update the `host` and `port` values for the `REST` elastic index writer:

        <writer id="indexer_elastic_rest_1" class="org.apache.nutch.indexwriter.elasticrest.ElasticRestIndexWriter">
        <parameters>
          <param name="host" value="localhost"/>
          <param name="port" value="9201"/>
          ...

11. Configure the elastic nutch plugin in `$LIFERAY_HOME/apache-nutch-1.15/conf/nutch-default.xml`. Update the `plugin.includes` property:

        <property>
          <name>plugin.includes</name>
          <value>protocol-http|urlfilter-(regex|validator)|parse-(html|tika)|index-(basic|anchor)|indexer-elastic-rest|scoring-opic|urlnormalizer-(pass|regex|basic)</value>
          ...

by changing the value `indexer-solr` to `indexer-elastic-rest`

12. Copy this config file to your portal server's `/osgi/configs` directory:
- [com.liferay.portal.search.elasticsearch6.internal.connection.FederatedElasticsearchConnectionConfiguration-federated.config](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/com.liferay.portal.search.elasticsearch6.internal.connection.FederatedElasticsearchConnectionConfiguration-federated.config)

13. Set up and start an Elasticsearch 6.1.3 server using these configs:
- [elasticsearch.yml](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/A/elasticsearch.yml)

14. Set up and start a Kibana 6.1.3 server using these configs:
- [kibana.yml](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/A/kibana.yml)

15. Execute this command in the kibana server:
```
PUT liferay_page_index
{
  "mappings": {
    "doc": {
      "date_detection": false,
      "dynamic_templates": [{
        "template_all_text": {
          "mapping": {
            "store": true,
            "type": "text"
          },
          "match": "*"
        }
      }]
    }
  }
}
```

16. Go to a page and add a Search Bar portlet, a Search Results portlet, a Search Options portlet, and a Federated Search Results portlet.

17. Go to the Search Options portlet configuration and add `Federated Page Search` to the `current sources` list.

18. Go to the Federated Search Results portlet configuration and select the `Federated Page Search` source and the `Page` display style.

19. Create a page titled `one`. Go to the Configuration settings for the page. In the `SEO` tab, fill out the `HTML Title`, `Description`, and `Keywords` fields and save.

20. Add a Web Content Display portlet to page `one` and select an article to display. Also add a Blogs portlet to the page and create a blog entry.

21. Go to Control Panel > Configuration > Search. Click on the `Crawl` button. It will take about 30 seconds to crawl the site. The tomcat logs will let you know when crawling is complete.

22. Once crawling is complete, click the `Ingest` button. Ingestion should complete almost instantly.

23. Go back to the home page and use the Search Bar portlet to search for any of the words entered in the page's SEO section and/or words contained in the web content article/blog