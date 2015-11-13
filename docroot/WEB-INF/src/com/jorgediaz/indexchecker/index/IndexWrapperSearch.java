package com.jorgediaz.indexchecker.index;

import com.jorgediaz.indexchecker.data.Data;
import com.jorgediaz.indexchecker.model.IndexCheckerModel;
import com.jorgediaz.util.model.ModelUtil;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.BooleanQueryFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class IndexWrapperSearch extends IndexWrapper {

	public IndexWrapperSearch(long companyId) {
		this.companyId = companyId;
	}

	@Override
	public Set<Data> getClassNameData(IndexCheckerModel model) {

		Set<Data> indexData = new HashSet<Data>();

		SearchContext searchContext = new SearchContext();
		searchContext.setCompanyId(companyId);
		BooleanQuery contextQuery = BooleanQueryFactoryUtil.create(
			searchContext);
		contextQuery.addRequiredTerm(Field.COMPANY_ID, companyId);
		contextQuery.addRequiredTerm(
			Field.ENTRY_CLASS_NAME, model.getClassName());

		int indexSearchLimit = -1;

		try {
			indexSearchLimit = getIndexSearchLimit();

			Document[] docs = executeSearch(
				searchContext, contextQuery, 50000, 200000);

			if (docs != null) {
				for (int i = 0; i < docs.length; i++) {
					DocumentWrapper doc = new DocumentWrapperSearch(docs[i]);

					String entryClassName = doc.getEntryClassName();

					if ((entryClassName != null) &&
						entryClassName.equals(model.getClassName())) {

						Data data = new Data(model);
						data.init(doc);

						indexData.add(data);
					}
				}
			}
		}
		catch (Exception e) {
			_log.error("EXCEPTION: " + e.getClass() + " - " + e.getMessage(),e);
		}
		finally {
			if (indexSearchLimit != -1) {
				try {
					setIndexSearchLimit(indexSearchLimit);
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Error restoring INDEX_SEARCH_LIMIT: " +
								e.getMessage(), e);
					}
				}
			}
		}

		return indexData;
	}

	@Override
	public Map<Long, Set<Data>> getClassNameDataByGroupId(
		IndexCheckerModel model) {

		Map<Long, Set<Data>> indexData = new HashMap<Long, Set<Data>>();

		SearchContext searchContext = new SearchContext();
		searchContext.setCompanyId(companyId);
		BooleanQuery contextQuery = BooleanQueryFactoryUtil.create(
			searchContext);
		contextQuery.addRequiredTerm(Field.COMPANY_ID, companyId);
		contextQuery.addRequiredTerm(
			Field.ENTRY_CLASS_NAME, model.getClassName());

		try {
			Hits hits = SearchEngineUtil.search(searchContext, contextQuery);

			Document[] docs = hits.getDocs();

			if (docs != null) {
				for (int i = 0; i < docs.length; i++) {
					DocumentWrapper doc = new DocumentWrapperSearch(docs[i]);

					String entryClassName = doc.getEntryClassName();

					if ((entryClassName != null) &&
						entryClassName.equals(model.getClassName())) {

						Data data = new Data(model);
						data.init(doc);

						Long groupId = data.getGroupId();

						Set<Data> indexDataSet = indexData.get(groupId);

						if (indexDataSet == null) {
							indexDataSet = new HashSet<Data>();
							indexData.put(groupId, indexDataSet);
						}

						indexDataSet.add(data);
					}
				}
			}
		}
		catch (Exception e) {
			_log.error(
				"EXCEPTION: " + e.getClass() + " - " + e.getMessage(), e);
		}

		return indexData;
	}

	@Override
	public Set<String> getTermValues(String term) {

		// TODO Pendiente

		Set<String> values = new HashSet<String>();
		values.add("Only implemented for 'Lucene' index wrapper");
		return values;
	}

	@Override
	public int numDocs() {

		// TODO Pendiente

		return -1;
	}

	protected Document[] executeSearch(
			SearchContext searchContext, BooleanQuery contextQuery, int start,
			int step)
		throws Exception, SearchException {

		for (int i = 0;; i++) {
			if (_log.isDebugEnabled()) {
				_log.debug("SetIndexSearchLimit: " + (start + step*i));
			}

			setIndexSearchLimit(start + step*i);

			Hits hits = SearchEngineUtil.search(searchContext, contextQuery);

			Document[] docs = hits.getDocs();

			if (docs.length < (start + step*i)) {
				return docs;
			}
		}
	}

	protected int getIndexSearchLimit() throws Exception {
		Class<?> propsValues =
			PortalClassLoaderUtil.getClassLoader().loadClass(
				"com.liferay.portal.util.PropsValues");

		java.lang.reflect.Field indexSearchLimitFiled =
			propsValues.getDeclaredField("INDEX_SEARCH_LIMIT");

		return (Integer)indexSearchLimitFiled.get(null);
	}

	protected void setIndexSearchLimit(int indexSearchLimit) throws Exception {
		Class<?> propsValues =
			PortalClassLoaderUtil.getClassLoader().loadClass(
				"com.liferay.portal.util.PropsValues");

		/* TODO: Los plugins de SOLR usan otra constante propia!!! */

		java.lang.reflect.Field indexSearchLimitFiled =
			propsValues.getDeclaredField("INDEX_SEARCH_LIMIT");
		ModelUtil.setFieldValue(null, indexSearchLimitFiled, indexSearchLimit);
	}

	private static Log _log = LogFactoryUtil.getLog(IndexWrapperSearch.class);

	private long companyId;

}