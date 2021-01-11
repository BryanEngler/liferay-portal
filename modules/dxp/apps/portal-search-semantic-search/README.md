## Similarity Search - Proof of Concept

1. In your Elastic 7 server edit the `elasticsearch.yml` file and add this setting:

- `path.repo: ["backup_repo"]`

2. Start elasticsearch and kibana

3. Check out this branch and open the [`META-INF`](https://github.com/BryanEngler/liferay-portal/tree/2020_12_10_LPS_122921_semantic_search_poc/modules/dxp/apps/portal-search-semantic-search/portal-search-semantic-search/src/main/resources/META-INF) folder

4. Copy the [`my_backup_location`](https://github.com/BryanEngler/liferay-portal/tree/2020_12_10_LPS_122921_semantic_search_poc/modules/dxp/apps/portal-search-semantic-search/portal-search-semantic-search/src/main/resources/META-INF/my_backup_location) folder into the `backup_repo` directory of your elastic server

5. In kibana, run:
```
PUT /_snapshot/my_backup
{
  "type": "fs",
  "settings": {
    "location": "my_backup_location"
  }
}
```

6. In kibana, run:
```
POST /_snapshot/my_backup/posts/_restore
```

7. In kibana, run:
```
GET _cat/indices
```
Confirm that the `posts` index has been imported into the cluster

8. Copy the [`com.liferay.portal.search.semantic.search.internal.configuration.TextEmbedderConfiguration.config`](https://github.com/BryanEngler/liferay-portal/blob/2020_12_10_LPS_122921_semantic_search_poc/modules/dxp/apps/portal-search-semantic-search/portal-search-semantic-search/src/main/resources/META-INF/com.liferay.portal.search.semantic.search.internal.configuration.TextEmbedderConfiguration.config) file into your `liferay_home/osgi/configs` directory and set the path for the [`embed.py`](https://github.com/BryanEngler/liferay-portal/blob/2020_12_10_LPS_122921_semantic_search_poc/modules/dxp/apps/portal-search-semantic-search/portal-search-semantic-search/src/main/resources/META-INF/embed.py) script location

9. Install python3.8 and pip3 on your computer

10. Open a terminal in the [`META-INF`](https://github.com/BryanEngler/liferay-portal/tree/2020_12_10_LPS_122921_semantic_search_poc/modules/dxp/apps/portal-search-semantic-search/portal-search-semantic-search/src/main/resources/META-INF) directory and run:
```
pip3 install -r requirements.txt
```

11. Confirm that the `embed.py` script will start, run:
```
python3.8 embed.py
```

The script should print "Google Universal Sentence Encoder Loaded" and any subsequent command line input will be converted to a Float array. Once you confirm that the script will run it can be terminated, it is not necessary to be running outside of portal. On startup, portal will start the script in its own process.

12. Deploy these modules:
- `portal-search-api`
- `portal-search-elasticsearch7-impl`
- `portal-search-web`
- `portal-search`
- `portal-search-semantic-search`

13. Start portal

14. Go to Publishing > Import. Import the [`search_page.lar`](https://github.com/BryanEngler/liferay-portal/blob/2020_12_10_LPS_122921_semantic_search_poc/modules/dxp/apps/portal-search-semantic-search/portal-search-semantic-search/src/main/resources/META-INF/search_page.lar) file

15. Go to the `Search` page and use the Search Bar portlet to query the `posts` index. The Search Results widget on the left will display "Match Query" results. The Search Results widget on the right will display "Similarity Search" results.

---

Reference: https://www.elastic.co/blog/text-similarity-search-with-vectors-in-elasticsearch and https://github.com/jtibshirani/text-embeddings