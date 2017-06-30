/**
 * Copyright (c) 2015-present Jorge Díaz All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package jorgediazest.indexchecker.model;

import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionList;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jorgediazest.indexchecker.index.IndexSearchUtil;

import jorgediazest.util.data.Data;
import jorgediazest.util.data.DataComparator;
import jorgediazest.util.data.DataUtil;
import jorgediazest.util.model.Model;
import jorgediazest.util.service.Service;

/**
 * @author Jorge Díaz
 */
public class JournalArticleQuery extends IndexCheckerModelQuery {

	public void addMissingJournalArticles(
			String[] attributes, Criterion filter, Criterion filterStatus,
			Map<Long, Data> dataMap)
		throws Exception {

		Service service = getModel().getService();
		DynamicQuery query = service.newDynamicQuery();

		List<String> validAttributes = new ArrayList<String>();

		ProjectionList projectionList = getModel().getPropertyProjection(
			attributes, validAttributes, null);

		query.setProjection(ProjectionFactoryUtil.distinct(projectionList));

		query.add(filter);

		DynamicQuery articleVersionDynamicQuery = service.newDynamicQuery(
			"articleVersion");

		articleVersionDynamicQuery.setProjection(
			ProjectionFactoryUtil.alias(
				ProjectionFactoryUtil.max(
					"articleVersion.version"), "articleVersion.version"));

		// We need to use the "this" default alias to make sure the database
		// engine handles this subquery as a correlated subquery

		articleVersionDynamicQuery.add(
			RestrictionsFactoryUtil.eqProperty(
				"this.resourcePrimKey", "articleVersion.resourcePrimKey"));

		articleVersionDynamicQuery.add(filterStatus);

		query.add(
			getModel().getProperty("version").eq(articleVersionDynamicQuery));

		query.add(filterStatus);

		@SuppressWarnings("unchecked")
		List<Object[]> results = (List<Object[]>)service.executeDynamicQuery(
			query);

		String[] validAttributesArr = validAttributes.toArray(
			new String[validAttributes.size()]);

		for (Object[] result : results) {
			Data data = DataUtil.createDataObject(
				model, dataComparator, validAttributesArr, result);

			if (!dataMap.containsKey(data.getResourcePrimKey())) {
				dataMap.put(data.getResourcePrimKey(), data);
			}
		}
	}

	@Override
	public void fillDataObject(Data data, String[] attributes, Document doc) {
		super.fillDataObject(data, attributes, doc);

		if (indexAllVersions) {
			long id = IndexSearchUtil.getIdFromUID(doc.get(Field.UID));
			data.setPrimaryKey(id);
		}
	}

	@Override
	public Map<Long, Data> getData(
			String[] attributes, String mapKeyAttribute, Criterion filter)
		throws Exception {

		if (indexAllVersions) {
			return super.getData(attributes, mapKeyAttribute, filter);
		}

		Map<Long, Data> dataMap = new HashMap<Long, Data>();

		Criterion filterStatusApproved = getModel().generateCriterionFilter(
			"status=" + WorkflowConstants.STATUS_APPROVED + "+status=" +
				WorkflowConstants.STATUS_IN_TRASH);

		addMissingJournalArticles(
			attributes, filter, filterStatusApproved, dataMap);

		Criterion filterStatusNotApproved = getModel().generateCriterionFilter(
			"status<>" + WorkflowConstants.STATUS_APPROVED + ",status<>" +
				WorkflowConstants.STATUS_IN_TRASH);

		addMissingJournalArticles(
			attributes, filter, filterStatusNotApproved, dataMap);

		Map<Long, Data> dataMap2 = new HashMap<Long, Data>();

		for (Data data : dataMap.values()) {
			dataMap2.put((Long)data.get(mapKeyAttribute), data);
		}

		return dataMap2;
	}

	@Override
	public void init(Model model, DataComparator dataComparator)
		throws Exception {

		super.init(model, dataComparator);

		try {
			indexAllVersions =
				PrefsPropsUtil.getBoolean(
					"journal.articles.index.all.versions");
		}
		catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean indexAllVersions;

}