**[CollectView](https://github.com/contiki-os/contiki/tree/master/tools/collect-view)** is a Contiki OS Java tool that makes possible to program and deploy a Contiki wireless sensor network and collect loads of information without even having to start programming. The main goal of this project was to add support in ContikiOS for [this](http://www.advanticsys.com/shop/wireless-sensor-networks-802154-sensor-boards-c-7_5.html) set of attachable sensor boards for the WSN device [CM5000](http://www.advanticsys.com/shop/mtmcm5000msp-p-14.html), a popular device based on the original open-source TelosB / Tmote Sky platform. The basic outlined workflow involved three steps:

#### 1. Drivers ####

The first step was writing the ContikiOS driver to read the sensor values of the attachable sensor board. Because of Contiki OS great driver interface modular design, this step was pretty much straightforward, as we didn't want to change the ADC12 configuration ([`sky-sensors.c`](https://github.com/contiki-os/contiki/blob/master/platform/sky/dev/sky-sensors.c)). 

The code for the drivers can be found [here](https://github.com/ediazal/sky-boards-collect/tree/master/contiki/platform/sky/dev). The drivers for the rest of the attachable boards (EM1000, SE1000, CO1000) are almost identical.

#### 2. Firmware ####

Next we had to modify the ContikiOS firmware which is loaded into the WSN nodes to send the right values to the base station CollectView application. It was again very easy to do so, thanks to the good ContikiOS firmware design practices. The firmwares can be found [here](https://github.com/ediazal/sky-boards-collect/tree/master/contiki/examples/sky-boards-collect). The firmwares for the rest of the attachable boards (EM1000, SE1000, CO1000) follow the same pattern.

#### 3. Base Station application ####

Last step revolved around modifying the CollectView base station application in order to support the attachable sensor boards. This step was not so easy as the CollectView application was designed for only one platform (Tmote sky) and with very few extensibility in mind. 

I extended the application design to support not only the CM5000-attachable sensor boards set, but any ContikiOS supported platform/device. See [this](https://github.com/ediazal/sky-boards-collect/wiki/AddPlatform) Wiki page for a brief guideline on how to use the CollectView application with any WSN platform. If by any chance you find the previous guide useful, you might want to read [this](https://github.com/ediazal/sky-boards-collect/wiki/CoojaCollect) too in order to work with the [Cooja](https://github.com/contiki-os/contiki/wiki/An-Introduction-to-Cooja) Network Simulator. 

I also added other features to the CollectView application:

* Data feeding to Cosm (now [Xively](http://Xively.com)) and [open.sen.se](http://open.sen.se) (not sure if it still works)
* Visualization of last received values for each node on the sensor map
* Load any firmware to motes (placed under [`firmware`](https://github.com/ediazal/sky-boards-collect/tree/master/sky-boards-collect/firmware) folder).
* Software calibration tool (this is an experimental feature)

[Screenshots](https://github.com/ediazal/sky-boards-collect/wiki/Screenshots).


# About #

This work was part of a final year project about Wireless Sensor Networks on Contiki OS.

For further information about Contiki OS, please refer to Contiki [website](http://www.contiki-os.org/) and [wiki](https://github.com/contiki-os/contiki/wiki).



