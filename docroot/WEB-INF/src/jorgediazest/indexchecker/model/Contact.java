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

import com.liferay.portal.kernel.dao.orm.Conjunction;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.model.User;

import java.util.List;

import jorgediazest.util.model.Model;

/**
 * @author Jorge Díaz
 */
public class Contact extends IndexCheckerModel {

	@Override
	public Criterion generateQueryFilter() {

		Conjunction conjunction = RestrictionsFactoryUtil.conjunction();

		Model modelUser = getModelFactory().getModelObject(User.class);

		DynamicQuery userDynamicQuery =
			modelUser.getService().newDynamicQuery();

		userDynamicQuery.setProjection(
			modelUser.getPropertyProjection("userId"));

		userDynamicQuery.add(
			modelUser.generateCriterionFilter(
				"defaultUser=false,status="+WorkflowConstants.STATUS_APPROVED));

		try {
			@SuppressWarnings("unchecked")
			List<Long> users = (List<Long>)
					modelUser.getService().executeDynamicQuery(
						userDynamicQuery);

			conjunction.add(generateInCriteria("classPK",users));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return conjunction;
	}

}