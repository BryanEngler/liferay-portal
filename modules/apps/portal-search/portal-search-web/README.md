### Federated Search Demo

Steps:
1) Build this branch. The modules that were changed/added are:
- modules/apps/portal-search-elasticsearch6/portal-search-elasticsearch6-impl
- modules/apps/portal-search/portal-search-admin-web
- modules/apps/portal-search/portal-search-api
- modules/apps/portal-search/portal-search-engine-adapter-api
- modules/apps/portal-search/portal-search-page-search
- modules/apps/portal-search/portal-search-web-api
- modules/apps/portal-search/portal-search-web

2) Set up and start 2 Elasticsearch 6.1.3 servers using these configs:
- A) [elasticsearch.yml](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/A/elasticsearch.yml)
- B) [elasticsearch.yml](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/B/elasticsearch.yml)

3) Set up and start 2 Kibana 6.1.3 servers using these configs:
- A) [kibana.yml](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/A/kibana.yml)
- B) [kibana.yml](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/B/kibana.yml)

4) Execute these commands in the kibana "A" server:
```
PUT federated_youtube
{
  "mappings": {
    "doc": {
      "properties": {
        "regularTextTitle": {
          "store": true,
          "type": "text"
        },
        "regularTextContent": {
          "store": true,
          "type": "text"
        },
        "youtubeVideoID": {
          "store": true,
          "type": "keyword"
        }
      }
    }
  }
}

PUT federated_youtube/doc/1
{
  "regularTextTitle" : "Dunkin Donuts 10,000(+) Calorie Challenge",
  "regularTextContent" : "Over 10,000 Calories of Dunkin Donut food!!",
  "youtubeVideoID" : "5E40mf0MNb0"
}

PUT federated_youtube/doc/2
{
  "regularTextTitle" : "Donut (feat. @viakavish)",
  "regularTextContent" : "Donut will take over the world.",
  "youtubeVideoID" : "YU4tH4saNk8"
}

PUT federated_youtube/doc/3
{
  "regularTextTitle" : "68 Donuts In 15 Min Family Challenge",
  "regularTextContent" : "Another Insane donut challenge, 68 donuts in 15 min with my whole family challenge!",
  "youtubeVideoID" : "bjDN9hXmadY"
}
```

5) Execute these commands in the kibana "B" server:
```
PUT federated_cluster_two_youtube
{
  "mappings": {
    "doc": {
      "properties": {
        "federatedTitle": {
          "store": true,
          "type": "text"
        },
        "federatedContent": {
          "store": true,
          "type": "text"
        },
        "youtubeVideoID": {
          "store": true,
          "type": "keyword"
        }
      }
    }
  }
}

PUT federated_cluster_two_youtube/doc/1
{
  "federatedTitle" : "How Krispy Kreme Doughnuts Are Made",
  "federatedContent" : "Have a donut. Or two. Or three.",
  "youtubeVideoID" : "Rn0XsW2l4d4"
}

PUT federated_cluster_two_youtube/doc/2
{
  "federatedTitle" : "$1 Donut Vs. $100 Donut",
  "federatedContent" : "This is truly the best donut I have ever had.",
  "youtubeVideoID" : "0n89GZvmeXI"
}

```
6) Copy these 2 config files to your portal server's `/osgi/configs` directory:
- A) [com.liferay.portal.search.elasticsearch6.internal.connection.FederatedElasticsearchConnectionConfiguration-federated.config](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/com.liferay.portal.search.elasticsearch6.internal.connection.FederatedElasticsearchConnectionConfiguration-federated.config)
- B) [com.liferay.portal.search.elasticsearch6.internal.connection.FederatedElasticsearchConnectionConfiguration-federated_cluster_two.config](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/com.liferay.portal.search.elasticsearch6.internal.connection.FederatedElasticsearchConnectionConfiguration-federated_cluster_two.config)

7) Start up portal, go to Control Panel > Server Administration > Script. Run these two groovy scripts to create portal content.
- A) [document.groovy](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/document.groovy)
- B) [webcontent.groovy](https://github.com/BryanEngler/liferay-portal/blob/federated_search/modules/apps/portal-search/portal-search-web/src/main/resources/configs/webcontent.groovy)

8) Go to a page and add a Search Bar portlet, a Search Results portlet, a Search Options portlet, and two Federated Search Results portlets.

9) Configure the Search Options portlet to enable Federated Search, adding the `Federated One` and `Federated Cluster Two` sources to the `current sources` list. Configure the Federated Search Results portlets to display results from each source respectively, using the `video` and/or `standard` display styles.

10) Search for `donut`
