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

import jorgediazest.util.model.Model;
import jorgediazest.util.model.ModelFactory;

/**
 * @author Jorge Díaz
 */
public class IndexCheckerModelFactory extends ModelFactory {

	public IndexCheckerModelFactory() {
		super(indexCheckerClassFactory);
	}

	protected static ModelClassFactory indexCheckerClassFactory =
		new ModelClassFactory() {

		public final String ASSET_ENTRY =
			"com.liferay.asset.kernel.model.AssetEntry";
		public final String CALENDAR_BOOKING =
			"com.liferay.calendar.model.CalendarBooking";
		public final String CONTACT = "com.liferay.portal.kernel.model.Contact";
		public final String DL_FILE_ENTRY_METADATA =
			"com.liferay.document.library.kernel.model.DLFileEntryMetadata";
		public final String EXPORT_IMPORT_CONFIGURATION =
			"com.liferay.exportimport.kernel.model.ExportImportConfiguration";
		public final String JOURNAL_ARTICLE =
			"com.liferay.journal.model.JournalArticle";
		public final String MB_MESSAGE =
			"com.liferay.message.boards.kernel.model.MBMessage";
		public final String TRASH_ENTRY =
			"com.liferay.trash.kernel.model.TrashEntry";
		public final String USER = "com.liferay.portal.kernel.model.User";
		public final String WIKI_NODE = "com.liferay.wiki.model.WikiNode";
		public final String WIKI_PAGE = "com.liferay.wiki.model.WikiPage";

		@Override
		public Class<? extends Model> getModelClass(String className) {
			if (ASSET_ENTRY.equals(className)) {
				return NotIndexed.class;
			}
			else if (CALENDAR_BOOKING.equals(className)) {
				return CalendarBooking.class;
			}
			else if (CONTACT.equals(className)) {
				return Contact.class;
			}
			else if (DL_FILE_ENTRY_METADATA.equals(className)) {
				return NotIndexed.class;
			}
			else if (EXPORT_IMPORT_CONFIGURATION.equals(className)) {
				return ExportImportConfiguration.class;
			}
			else if (JOURNAL_ARTICLE.equals(className)) {
				return JournalArticle.class;
			}
			else if (MB_MESSAGE.equals(className)) {
				return MBMessage.class;
			}
			else if (TRASH_ENTRY.equals(className)) {
				return NotIndexed.class;
			}
			else if (USER.equals(className)) {
				return User.class;
			}
			else if (WIKI_NODE.equals(className)) {
				return WikiNode.class;
			}
			else if (WIKI_PAGE.equals(className)) {
				return WikiPage.class;
			}
			else {
				return IndexCheckerModel.class;
			}
		}

	};

}