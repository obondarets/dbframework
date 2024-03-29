package edu.gui.table;

import edu.dbframework.parse.beans.ColumnItem;
import edu.dbframework.parse.beans.TableItem;
import edu.dbframework.parse.helpers.DatabaseManager;
import edu.gui.Main;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataTable extends JTable {

    DatabaseManager databaseManager = (DatabaseManager) Main.context.getBean("databaseManager");
    DataTableManager tableManager = new DataTableManager();

    public DataTable() {
        super();
        this.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        this.setPreferredScrollableViewportSize(new Dimension(800, 600));
        this.setFillsViewportHeight(true);
        this.setBorder(BorderFactory.createLineBorder(Color.gray));
    }

    public DataTable(DataTableModel tableModel) {
        super(tableModel);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        this.setPreferredScrollableViewportSize(new Dimension(800, 600));
        this.setFillsViewportHeight(true);
        this.setBorder(BorderFactory.createLineBorder(Color.gray));
        renderListeners();
    }

    public void setDataTableModel(TableModel dataModel) {
        super.setModel(dataModel);
        renderListeners();
    }

    public void renderListeners() {
        removeAllListeners();
        prepareIncomingRelationsListeners();
        prepareOutgoingRelationsListener();
    }

    private void prepareOutgoingRelationsListener() {
        /* different color for outgoing relation column */
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBackground(Color.GREEN);

        final DataTableModel model = (DataTableModel) this.getModel();
        final Map<Integer, ColumnItem> relationColumnsByIndex = model.getOutgoingColumnsByIndex();

        // different color for header
        for (Integer index : relationColumnsByIndex.keySet()) {
            getColumnModel().getColumn(index).setHeaderRenderer(cellRenderer);
        }

        this.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = convertColumnIndexToModel(columnAtPoint(e.getPoint()));
                if (relationColumnsByIndex.containsKey(index)) {
                    int[] selectedRows = DataTable.this.getSelectedRows();
                    ColumnItem columnItem = relationColumnsByIndex.get(index);
//                    TableItem creatingTableItem = databaseManager.getDatabaseBean().getTableByName(columnItem.getRelationTableName());
                    if (selectedRows.length >= 1) {
                        List<String> rows = getSelectedPrimaryKeys();// new ArrayList<String>();
                        setDataTableModel(tableManager.getOutgoingRelationDataModel(model.getTableItem(), rows, columnItem));
                    } else {
//!!!                        setDataTableModel(tableManager.getTableItemDataModel(creatingTableItem));
                    }
                }
            }
        });
    }

    private void prepareIncomingRelationsListeners() {

        final DataTableModel model = (DataTableModel) this.getModel();
        final TableItem tableItem = model.getTableItem();

        if (this.getModel().getColumnCount() > tableItem.getColumns().size()) {
            /* different color for incoming relation columns */
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
            cellRenderer.setBackground(Color.YELLOW);

            List<Integer> relationColumnIndexes = model.getIncomingColumnIndexes();
            // different color for header
            for (Integer index : relationColumnIndexes) {
                getColumnModel().getColumn(index).setHeaderRenderer(cellRenderer);
            }
            addRowClickListener(model, tableItem);
            addHeaderListener(model, tableItem);
        }
    }

    private void addRowClickListener(final DataTableModel model, final TableItem tableItem) {
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedColumn = getColumnModel().getSelectionModel().getLeadSelectionIndex();
                    int columnCount = tableItem.getColumns().size();
                    for (int i = columnCount; i < model.getColumnCount(); i++) {
                        if (getColumnModel().getSelectionModel().getLeadSelectionIndex() == i) {
                            // in incoming relations by default column name is table name7
                            String tableName = model.getTableNameForColumn(selectedColumn);// DataTable.this.getColumnName(selectedColumn);
                            TableItem relationTableItem = databaseManager.getDatabaseBean().getTableByName(tableName);

                            String relationColumnName = "";
                            for (ColumnItem columnItem : relationTableItem.getColumns()) {
                                if (columnItem.getRelationTableName() != null) {
                                    if (columnItem.getRelationTableName().equals(tableItem.getName())) {
                                        relationColumnName = columnItem.getName();
                                        break;
                                    }
                                }
                            }
                            List<String> primaryKey = new ArrayList<String>();
                            for (int j = 0; j < model.getColumnCount(); j++) {
                                if (model.getColumnName(j).equals(tableItem.getPrimaryKey().getName())) {
                                    primaryKey.add((String) model.getValueAt(DataTable.this.getSelectionModel().getLeadSelectionIndex(), j));
                                    break;
                                }
                            }
                            setDataTableModel(tableManager.getInternalRelationDataModel(relationTableItem, primaryKey, relationColumnName));
                        }
                    }
                }
            }
        });
    }

    private void addHeaderListener(final DataTableModel model, final TableItem tableItem) {
        final List<Integer> incomingColumnIndexes = model.getIncomingColumnIndexes();
        this.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = convertColumnIndexToModel(columnAtPoint(e.getPoint()));
                if (incomingColumnIndexes.contains(index)) {
                    List<String> selectedPrimaryKeys = getSelectedPrimaryKeys();
                    String tableName = model.getTableNameForColumn(index);//DataTable.this.getColumnName(index);
                    TableItem relationTableItem = databaseManager.getDatabaseBean().getTableByName(tableName);

                    String relationColumnName = "";
                    for (ColumnItem columnItem : relationTableItem.getColumns()) {
                        if (columnItem.getRelationTableName() != null) {
                            if (columnItem.getRelationTableName().equals(tableItem.getName())) {
                                relationColumnName = columnItem.getName();
                                break;
                            }
                        }
                    }
                    setDataTableModel(tableManager.getInternalRelationDataModel(relationTableItem, selectedPrimaryKeys, relationColumnName));
                }

            }
        });
    }

    private List<String> getSelectedPrimaryKeys() {
        List<String> keys = new ArrayList<String>();
        int[] selectedRows = this.getSelectedRows();
        DataTableModel model = (DataTableModel) this.getModel();
        int primaryKeyColumnIndex = 0;
        for (int i = 0; i < model.getTableItem().getColumns().size(); i++) {
            if (model.getTableItem().getColumns().get(i).getPrimaryKey()) {
                primaryKeyColumnIndex = i;
                break;
            }
        }

        if (selectedRows.length == 0) {
            for (int i = 0; i < this.getRowCount(); i++) {
                keys.add(this.getValueAt(i, primaryKeyColumnIndex).toString());
            }
        } else {
            for (Integer row : selectedRows) {
                keys.add(this.getValueAt(row, primaryKeyColumnIndex).toString());
            }
        }
        return keys;
    }

    private void removeAllListeners() {
        for (MouseListener ml : this.getTableHeader().getMouseListeners()) {
            if (!(ml  instanceof BasicTableHeaderUI.MouseInputHandler))
                this.getTableHeader().removeMouseListener(ml);
        }
    }
}
