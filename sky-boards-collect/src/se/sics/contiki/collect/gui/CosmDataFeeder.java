/*
 * DataFeederCosm
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 28 jul 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import se.sics.contiki.collect.Configurable;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.PublisherCosm;
import se.sics.contiki.collect.Sensor;
import se.sics.contiki.collect.SensorData;
import se.sics.contiki.collect.Visualizer;

public class CosmDataFeeder extends JPanel implements Visualizer, Configurable {

  private static final long serialVersionUID = 1L;
  String category;

  private CosmTableGUI cosmTableGUI;
  private CosmTableModel cosmTableModel;
  private Properties config;
  private String apiKey;
  
  private JTextArea logArea;
  private JButton addButton;
  private JButton deleteButton;
  private JPanel panel;
  private JPasswordField keyField;

  private Hashtable<String, Node> nodes = new Hashtable<String, Node>();

  public CosmDataFeeder(String category, Properties config) {
    panel = new JPanel(new BorderLayout());
    this.category = category;
    this.config = config;
    keyField = new JPasswordField();
    keyField.setColumns(30);

    addButton = new JButton("Add");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (nodes.isEmpty())
              return;
            JDialog dialog = new DialogAdd(nodes);
            dialog.setLocationRelativeTo(addButton);
            dialog.setVisible(true);
          }
        });
      }
    });

    deleteButton = new JButton("Delete");
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteSelectedRows();
      }
    });

    cosmTableModel = new CosmTableModel(config);
    cosmTableGUI = new CosmTableGUI(cosmTableModel);

    logArea = new JTextArea(10,40);
    logArea.setEditable(false);

    JPanel cosmPanel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(10, 20, 10, 0);
    cosmPanel.add(new JLabel("API key "), c);

    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(10, 0, 10, 20);
    keyField.setToolTipText("Copy your Cosm API key here");
    cosmPanel.add(keyField, c);

    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    cosmPanel.add(new JSeparator(), c);

    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 4;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.insets = new Insets(10, 20, 5, 20);
    cosmPanel.add(new JScrollPane(cosmTableGUI), c);

    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 4;
    c.weighty = 0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    JPanel remsetPanel = new JPanel();
    remsetPanel.add(addButton);
    remsetPanel.add(deleteButton);
    c.insets = new Insets(5, 20, 10, 20);
    cosmPanel.add(remsetPanel, c);

    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 4;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    cosmPanel.add(new JSeparator(), c);

    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 4;
    c.weighty = 0.5;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(10, 20, 10, 20);
    cosmPanel.add(new JScrollPane(logArea), c);
    
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem clearItem = new JMenuItem("Clear");
    clearItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        logArea.setText("");
      }
    });
    popupMenu.add(clearItem);
    logArea.setComponentPopupMenu(popupMenu);

    panel.add(cosmPanel, BorderLayout.CENTER);
  }

  @Override
  public void clearNodeData() {
    if (!isVisible())
      return;
    nodes.clear();
  }

  public String getCategory() {
    return category;
  }

  public String getTitle() {
    return "Cosm Feeder";
  }

  public Component getPanel() {
    return panel;
  }

  @Override
  public void nodeAdded(Node node) {
    if (!isVisible())
      return;
    String nodeID = node.getID();
    if (nodes.get(nodeID) != null)
      return;
    SensorData sd = node.getLastSD();
    if (sd == null)
      return; // unknown node type
    nodes.put(nodeID, node);
  }

  private Vector<String> getSortedNodeList() {
    Vector<Node> list = new Vector<Node>();
    for (Object key : nodes.keySet()) {
      list.add(nodes.get(key));
    }
    Node[] nodeList = list.toArray(new Node[0]);
    Arrays.sort(nodeList);
    return toStringList(nodeList);
  }

  private Vector<String> toStringList(Node[] nodeList) {
    Vector<String> list = new Vector<String>();
    for (int i = 0, n=nodeList.length; i < n; i++)
      list.add(nodeList[i].getID());
    return list;
  }

  @Override
  public void nodeDataReceived(SensorData sensorData) {

    if (nodes.get(sensorData.getNodeID()) == null) {
      nodeAdded(sensorData.getNode());
      return;
    }

    CosmRow row;
    Hashtable<String, String> feedTable = new Hashtable<String, String>();
    Node node = sensorData.getNode();
    String nodeId = node.getID();
    if (!getAPIKey())
      return;
    ArrayList<CosmRow> FeedRows = cosmTableModel.getRows(nodeId);
    if (FeedRows==null)
      return;
    ListIterator<CosmRow> it = FeedRows.listIterator();
    PublisherCosm publisher;

    while (it.hasNext()) {
      row = (CosmRow) it.next();
      if ((boolean) row.getField(CosmRow.IDX_SEND)) {
        putValues(node, row, feedTable);
        publisher = new PublisherCosm(feedTable, apiKey,
            (String) row.getField(CosmRow.IDX_FEEDID), this);
        publisher.setCosmTitle((String) row.getField(CosmRow.IDX_FEEDTITLE));
        publisher.setFeedingNode(nodeId);
        publisher.start();
      }
    }
  }

  private void putValues(Node node, CosmRow row,
      Hashtable<String, String> feedTable) {
    String value = "";
    @SuppressWarnings("unchecked")
    Hashtable<String, String> dataStreams = (Hashtable<String, String>) row
        .getField(CosmRow.IDX_DATASTREAMS);
    for (Object sensor : dataStreams.keySet()) {
      if (row.getField(CosmRow.IDX_CONV).equals("Converted")) {
        value = node.getRoundedConvOf((String) sensor);
      } else if (row.getField(CosmRow.IDX_CONV).equals("Raw")) {
        value = Double.toString(node.getLastValueOf((String) sensor));
      }
      feedTable.put(dataStreams.get(sensor), value);
    }
  }
  
  private boolean  getAPIKey(){
    apiKey = arrayToString(keyField.getPassword());
    if (apiKey == null || "".equals(apiKey)) {
      return false;
    }
    return true;
  }

  @Override
  public void nodesSelected(Node[] node) {
    cosmTableGUI.selectRows(node);
  }

  public void deleteSelectedRows() {
    int[] selectedRows = cosmTableGUI.getSelectedRows();
    if (selectedRows.length == 0)
      return;
    int opt = JOptionPane.showConfirmDialog(deleteButton, "Delete "
        + selectedRows.length + " row(s)?", "Confirm delete",
        JOptionPane.YES_NO_OPTION);
    if (opt == JOptionPane.YES_OPTION) {
      ArrayList<String> delList;
      delList = cosmTableModel.deleteRows(selectedRows);
      for (int i = 0, n=delList.size(); i < n ; i++) {
        config.remove("feedcosm," + delList.get(i));
      }
    }
  }

  public static String arrayToString(char[] a) {
    StringBuffer result = new StringBuffer();
    if (a.length > 0) {
      result.append(a[0]);
      for (int i = 1, n=a.length; i < n ; i++) {
        result.append(a[i]);
      }
    }
    return result.toString();
  }

  public void addResponseLine(final String text) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (logArea.getText().length()>4096) 
          logArea.setText("");
        logArea.append(text+"\n");
        logArea.setCaretPosition(logArea.getText().length());
      }
    });
  }

  /**
   * Configuration line format (key=value)
   * 
   * feedcosm,<feedId> = <node>,<feedTitle>,<conv>,<send>,{<dataStreams>}
   * 
   */
  public void updateConfig(Properties config) {
    ListIterator<CosmRow> li = ((CosmTableModel) cosmTableGUI.getModel())
        .getListIterator();
    while (li.hasNext()) {
      CosmRow sr = li.next();
      StringBuilder value = new StringBuilder();
      @SuppressWarnings("unchecked")
      Hashtable<String, String> dataStreams = (Hashtable<String, String>) sr
          .getField(CosmRow.IDX_DATASTREAMS);
      String key = "feedcosm," + sr.getField(CosmRow.IDX_FEEDID);
      value
          .append(sr.getField(CosmRow.IDX_NODE) + ","
              + sr.getField(CosmRow.IDX_FEEDTITLE) + ","
              + sr.getField(CosmRow.IDX_CONV) + ","
              + sr.getField(CosmRow.IDX_SEND));
      value.append("," + dataStreams.toString());
      config.setProperty(key, value.toString());
    }
  }

  public void loadConfigLine(String key, String value) {
    Hashtable<String, String> dataStreams;

    dataStreams = new Hashtable<String, String>();
    String[] SKey = key.split(",");
    String[] SVal = value.split(",", 5);
    boolean send = Boolean.parseBoolean(SVal[3]);
    parseDataStreams(dataStreams, SVal[4]);
    cosmTableModel
        .addRow(SVal[0], dataStreams, SKey[1], SVal[1], SVal[2], send);

  }

  private void parseDataStreams(Hashtable<String, String> dataStreams,
      String strds) {
    strds = strds.substring(0, strds.length() - 1);
    strds = strds.substring(1);
    String splited[] = strds.split(",");
    for (int i = 0, n=splited.length; i < n; i++) {
      String val[] = splited[i].trim().split("=");
      dataStreams.put(val[0], val[1]);
    }
  }

  private class DialogAdd extends JDialog {
    private static final long serialVersionUID = 1L;
    private JComboBox<String> comboBoxNode;
    private JComboBox<String> comboBoxRaw;
    private Hashtable<String, String> dataStreams;
    JTextField feedIdField;
    JTextField feedTitleField;
    String feedConv = "Converted";
    String feedingNode;
    JButton OKbutton;
    JPanel pane;

    public DialogAdd(final Hashtable<String, Node> nodes) {
      pane = new JPanel();
      pane.setLayout(new GridBagLayout());
      pane.setOpaque(true);

      feedIdField = new JTextField();
      feedTitleField = new JTextField();
      dataStreams = new Hashtable<String, String>();
      OKbutton = new JButton("Add");
      OKbutton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String id = feedIdField.getText();
          String title = feedTitleField.getText();
          title.replaceAll(",", " ");
          if (!isValidFeedID(id)) {
            JOptionPane.showMessageDialog(pane, "Invalid feed ID", "Error",
                JOptionPane.ERROR_MESSAGE);
          } else if (!isValidFeedTitle(title)) {
            JOptionPane.showMessageDialog(pane,
                "Feed title cannot be blank or contain \",\" character",
                "Error", JOptionPane.ERROR_MESSAGE);
          } else {
            cosmTableModel.addRow(feedingNode, dataStreams, id, title,
                feedConv, true);
            closeWindow();
          }
        }
      });
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          /* DEBUG LINES */
          /* DEBUG LINES */
          closeWindow();
        }
      });
      comboBoxNode = new JComboBox<String>();
      comboBoxNode.setModel(new DefaultComboBoxModel<String>(
          getSortedNodeList()));
      comboBoxNode.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (comboBoxNode.getItemCount() == 0)
            return;
          int idx = comboBoxNode.getSelectedIndex();
          feedingNode = comboBoxNode.getItemAt(idx).toString();
          Node n = nodes.get(feedingNode);
          Sensor[] sensors = n.getSensors();
          dataStreams.clear();
          for (int i = 0, len=sensors.length; i < len; i++) {
            String sensorId = sensors[i].getId();
            dataStreams.put(sensorId, sensorId);
          }
        }
      });

      String[] opt = { "Converted", "Raw" };
      comboBoxRaw = new JComboBox<String>();
      comboBoxRaw.setModel(new DefaultComboBoxModel<String>(opt));
      comboBoxRaw.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int idx = comboBoxRaw.getSelectedIndex();
          feedConv = comboBoxRaw.getItemAt(idx).toString();
        }
      });

      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 0;
      c.weighty = 0.1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 0, 0, 0);
      pane.add(new JLabel("Node"), c);

      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(comboBoxNode, c);

      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 10, 0, 0);
      pane.add(new JLabel("Feed identifier"), c);

      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(feedIdField, c);

      c.gridx = 0;
      c.gridy = 2;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 0, 0, 0);
      pane.add(new JLabel("Feed Title"), c);

      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(feedTitleField, c);

      c.gridx = 0;
      c.gridy = 3;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.LINE_END;
      c.insets = new Insets(10, 0, 0, 0);
      pane.add(new JLabel("Send values"), c);

      c.gridx = 1;
      c.gridy = 3;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.insets = new Insets(10, 5, 0, 10);
      pane.add(comboBoxRaw, c);

      c.gridx = 0;
      c.gridy = 4;
      c.gridwidth = 2;
      c.weightx = 0.1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      JPanel groupPanel = new JPanel();
      groupPanel.add(OKbutton);
      groupPanel.add(cancelButton);
      c.insets = new Insets(10, 10, 10, 10);
      pane.add(groupPanel, c);

      setContentPane(pane);
      if (comboBoxNode.getItemCount() > 0)
        comboBoxNode.setSelectedIndex(0);
      pack();
      setTitle("Feed configuration");
      setModalityType(ModalityType.APPLICATION_MODAL);
    }

    void closeWindow() {
      dispose();
    }
  }

  public static boolean isValidFeedID(String id) {
    if (id == null || id.equals("") || !isInteger(id)
        || Integer.valueOf(id) < 0)
      return false;
    return true;
  }

  public static boolean isValidFeedTitle(String title) {
    if (title.contains(",") || title.equals(""))
      return false;
    return true;
  }

  public static boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
