'use strict';

// require WinCC OA interface
const { WinccoaManager, WinccoaSecurityEventId, WinccoaConnectUpdateType } = require('winccoa-manager');
global.scada = new WinccoaManager();

// Node.js communication to Java
global.node = require('./node');

// Main Java object for communication
global.java = new (Java.type('com.winccoa.Winccoa'))();
global.java.test();

setInterval(() => java.loop(), 1);

setTimeout(() => scada.exit(), 10000);