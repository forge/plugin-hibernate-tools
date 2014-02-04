package org.hibernate.forge.generate;

import java.util.Collections;
import java.util.List;

import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.SchemaSelection;
import org.hibernate.forge.common.Constants;
import org.hibernate.util.StringHelper;

/**
 * Defines a {@link SchemaSelection} limitation on reverse engineering strategy.
 * 
 * @author jpereira - Linkare TI
 * 
 */
public class DefaultReverseEngineeringStrategyWithSchemaSelection extends
		DefaultReverseEngineeringStrategy {

	private List<SchemaSelection> schemaSelections;

	/**
	 * Creates a new {@link ReverseEngineeringStrategy} with additional
	 * limitations by catalog, schema and table
	 * 
	 * @param catalogs
	 *            The catalogs to use ( FOO* matches any catalog named FOOX,
	 *            FOOBAR, FOO, etc)
	 * @param schemas
	 *            The schemas to use ( FOO* matches any schema named FOOX,
	 *            FOOBAR, FOO, etc)
	 * @param tables
	 *            The tables to use ( FOO* matches any table named FOOX, FOOBAR,
	 *            FOO, etc)
	 */
	public DefaultReverseEngineeringStrategyWithSchemaSelection(
			String catalogs, String schemas, String tables) {

		String catalog = catalogs == null ? Constants.CATALOG_DEFAULT
				: catalogs;
		String schema = schemas == null ? Constants.SCHEMA_DEFAULT : schemas;
		String table = tables == null ? Constants.TABLE_DEFAULT : tables;

		catalog = StringHelper.replace(catalog, "*", "%");
		schema = StringHelper.replace(schema, "*", "%");
		table = StringHelper.replace(table, "*", "%");

		SchemaSelection schemaSelection = new SchemaSelection(catalog, schema,
				table);
		schemaSelections = Collections.singletonList(schemaSelection);
	}

	/**
	 * Obtains the list of schema selections (built at construct time)
	 * 
	 * @return The schema limitations for this reverse engineering strategy
	 */
	@Override
	public List getSchemaSelections() {
		return schemaSelections;
	}
}
