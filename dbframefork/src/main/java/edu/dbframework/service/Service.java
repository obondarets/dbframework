package edu.dbframework.service;

import edu.dbframework.database.Dao;
import edu.dbframework.database.SqlQueryBuilder;
import edu.dbframework.parse.beans.items.ColumnItem;
import edu.dbframework.parse.beans.items.TableItem;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: GreenPoser
 * Date: 15.06.13
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class Service {

    private SqlQueryBuilder sqlQueryBuilder;
    private Dao dao;

    public Service() {
    }

    public Map<String, List<String>> getDataForTableItem(TableItem tableItem) {
        return dao.getData(sqlQueryBuilder.buildQueryForTableItem(tableItem));
    }

    public Map<String, List<String>> getDataByRows(TableItem tableItem, List<String> rows, ColumnItem bindingColumn, String relTable) {
        return dao.getData(sqlQueryBuilder.buildQueryByRows(tableItem, rows, bindingColumn, relTable));
    }

    public Map<String, List<String>> getDataByRelationColumn(TableItem tableItem, String primaryKey, String indexColumn) {
        return dao.getData(sqlQueryBuilder.buildQueryByRelationColumn(tableItem, primaryKey, indexColumn));
    }

    public SqlQueryBuilder getSqlQueryBuilder() {
        return sqlQueryBuilder;
    }

    public void setSqlQueryBuilder(SqlQueryBuilder sqlQueryBuilder) {
        this.sqlQueryBuilder = sqlQueryBuilder;
    }

    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }
}