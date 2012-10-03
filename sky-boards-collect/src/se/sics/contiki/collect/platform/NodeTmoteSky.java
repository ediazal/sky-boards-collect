/*
 * NodeTmoteSky
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 15 jun 2012
 */

package se.sics.contiki.collect.platform;

import se.sics.contiki.collect.sensor.*;
import se.sics.contiki.collect.Node;
import se.sics.contiki.collect.SensorInfo;
import se.sics.contiki.collect.Sensor;

public class NodeTmoteSky extends Node implements SensorInfo {

  public NodeTmoteSky(String nodeID) {
    super(nodeID);
    init();
  }
  
  public NodeTmoteSky(String nodeID, String nodeName){
    super(nodeID,nodeName);
    init();
  }
  
  @Override
  public void init() {
    addSensors();
    mapMsgFormat();
    setNodeType();
  }
  
  @Override
  public void addSensors() {
    String nodeID = this.getID();
    Sensor temp=new SHT11Temperature(TEMPERATURE_SENSOR, nodeID);
    Sensor hum=new SHT11Humidity(HUMIDITY_SENSOR, nodeID);
    sensors.put(LIGHT1_SENSOR, new S1087(LIGHT1_SENSOR, nodeID));
    sensors.put(LIGHT2_SENSOR, new S108701(LIGHT2_SENSOR, nodeID));   
    sensors.put(TEMPERATURE_SENSOR, temp);  
    sensors.put(HUMIDITY_SENSOR, hum);
    ((SHT11Humidity) hum).setAssociatedTempSensor(temp);
  }
  
  @Override
  public void mapMsgFormat(){
    dataMsgMapping.put(LIGHT1_SENSOR, LIGHT1);
    dataMsgMapping.put(LIGHT2_SENSOR, LIGHT2);
    dataMsgMapping.put(TEMPERATURE_SENSOR, TEMPERATURE);  
    dataMsgMapping.put(HUMIDITY_SENSOR, HUMIDITY);
  }

  @Override
  public void setNodeType() {
    type="TmoteSky";  
  }
}