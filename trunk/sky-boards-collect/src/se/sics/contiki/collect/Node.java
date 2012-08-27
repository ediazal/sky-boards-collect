/*
 * Copyright (c) 2008, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *
 * -----------------------------------------------------------------
 *
 * Node
 *
 * Authors : Joakim Eriksson, Niclas Finne
 * Created : 3 jul 2008
 *
 * Modified by Eloy Díaz, 30 jul 2012
 */

package se.sics.contiki.collect;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 */
public class Node implements Comparable<Node> {

	private static final boolean SINGLE_LINK = true;

	private SensorDataAggregator sensorDataAggregator;
	private ArrayList<SensorData> sensorDataList = new ArrayList<SensorData>();
	private ArrayList<Link> links = new ArrayList<Link>();

	private final String id;
	private final String name;

	private Hashtable<String, Object> objectTable;

	private long lastActive;
	protected Hashtable<Integer, NodeSensor> sensors = new Hashtable<Integer, NodeSensor>();
	private int nodeType;
	private String feedID;
	private String feedTitle;

	public Node(String nodeID) {
		this(nodeID, nodeID);
	}

	public Node(String nodeID, String nodeName) {
		this.id = nodeID;
		this.name = nodeName;
		feedID = null;
		sensorDataAggregator = new SensorDataAggregator(this);
		sensors = new Hashtable<Integer, NodeSensor>();
	}

	public final String getID() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public long getLastActive() {
		return lastActive;
	}

	public void setLastActive(long lastActive) {
		this.lastActive = lastActive;
	}

	@Override
	public int compareTo(Node o) {
		String i1 = id;
		String i2 = o.getID();
		// Shorter id first (4.0 before 10.0)
		if (i1.length() == i2.length()) {
			return i1.compareTo(i2);
		}
		return i1.length() - i2.length();
	}

	public String toString() {
		return name;
	}

	// -------------------------------------------------------------------
	// Attributes
	// -------------------------------------------------------------------

	public Object getAttribute(String key) {
		return getAttribute(key, null);
	}

	public Object getAttribute(String key, Object defaultValue) {
		if (objectTable == null) {
			return null;
		}
		Object val = objectTable.get(key);
		return val == null ? defaultValue : val;
	}

	public void setAttribute(String key, Object value) {
		if (objectTable == null) {
			objectTable = new Hashtable<String, Object>();
		}
		objectTable.put(key, value);
	}

	public void clearAttributes() {
		if (objectTable != null) {
			objectTable.clear();
		}
	}

	// -------------------------------------------------------------------
	// SensorData
	// -------------------------------------------------------------------

	public SensorDataAggregator getSensorDataAggregator() {
		return sensorDataAggregator;
	}

	public SensorData getLastSD() {
		int lastData = getSensorDataCount() - 1;
		if (lastData == -1)
			return null;

		return getSensorData(lastData);
	}

	public SensorData[] getAllSensorData() {
		return sensorDataList.toArray(new SensorData[sensorDataList.size()]);
	}

	public void removeAllSensorData() {
		sensorDataList.clear();
		sensorDataAggregator.clear();
	}

	public SensorData getSensorData(int index) {
		return sensorDataList.get(index);
	}

	public int getSensorDataCount() {
		return sensorDataList.size();
	}

	public boolean addSensorData(SensorData data) {
		if (sensorDataList.size() > 0) {
			SensorData last = sensorDataList.get(sensorDataList.size() - 1);
			if (data.getNodeTime() < last.getNodeTime()) {
				// Sensor data already added
				System.out.println("SensorData: ignoring (time "
				    + (data.getNodeTime() - last.getNodeTime()) + "msec): " + data);
				return false;
			}
		}
		sensorDataList.add(data);
		sensorDataAggregator.addSensorData(data);
		return true;
	}

	// -------------------------------------------------------------------
	// Links
	// -------------------------------------------------------------------

	public Link getLink(Node node) {
		for (Link l : links) {
			if (l.node == node) {
				return l;
			}
		}

		// Add new link
		Link l = new Link(node);
		if (SINGLE_LINK) {
			links.clear();
		}
		links.add(l);
		return l;
	}

	public Link getLink(int index) {
		return links.get(index);
	}

	public int getLinkCount() {
		return links.size();
	}

	public void removeLink(Node node) {
		for (int i = 0, n = links.size(); i < n; i++) {
			Link l = links.get(i);
			if (l.node == node) {
				links.remove(i);
				break;
			}
		}
	}

	public void clearLinks() {
		links.clear();
	}

	// -------------------------------------------------------------------
	// node sensors getters
	// -------------------------------------------------------------------

	public NodeSensor[] getNodeSensors() {
		NodeSensor[] sensors_array = new NodeSensor[sensors.size()];
		int i = sensors.size() - 1;
		for (Object key : sensors.keySet()) {
			sensors_array[i] = sensors.get(key);
			i--;
		}

		return sensors_array;
	}

	public NodeSensor getNodeSensor(int key) {
		return sensors.get(key);
	}

	public NodeSensor getNodeSensor(String name) {
		return sensors.get(keyConv(name));
	}

	static public int keyConv(String sensorName) {
		if (sensorName.equals("Light1"))
			return SensorInfo.LIGHT1;
		if (sensorName.equals("Light2"))
			return SensorInfo.LIGHT2;
		if (sensorName.equals("Temperature"))
			return SensorInfo.TEMPERATURE;
		if (sensorName.equals("Humidity"))
			return SensorInfo.HUMIDITY;
		if (sensorName.equals("CO"))
			return SensorInfo.CO;
		if (sensorName.equals("CO2"))
			return SensorInfo.CO2;
		if (sensorName.equals("Dust"))
			return SensorInfo.DUST;

		return -1;
	}

	public int getSensorsCount() {
		return sensors.size();
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public int getNodeType() {
		return nodeType;
	}

	// -------------------------------------------------------------------
	// Feed for Cosm
	// -------------------------------------------------------------------

	public void setFeedID(String feedID) {
		this.feedID = feedID;
	}

	public String getFeedID() {
		return feedID;
	}

	public void setFeedTitle(String feedTitle) {
		this.feedTitle = feedTitle;
	}

	public String getFeedTitle() {
		return feedTitle;
	}
}
