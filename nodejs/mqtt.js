'use strict';

// require WinCC OA interface
const { WinccoaManager } = require('winccoa-manager');
global.scada = new WinccoaManager();

// Node.js communication to Java
global.node = require('./node');

// Main Java object for communication
global.java = new (Java.type('com.winccoa.mqtt.MqttBroker'))();
function loop() { java.loop(); setTimeout(loop, 1); }; loop();