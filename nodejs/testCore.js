'use strict';

// require WinCC OA interface
const { WinccoaManager, WinccoaSecurityEventId, WinccoaConnectUpdateType } = require('winccoa-manager');
global.scada = new WinccoaManager();

// Node.js communication to Java
global.node = require('./node');

// Main Java object for communication
global.java = new (Java.type('com.winccoa.nodejs.WinccoaCore'))();
global.java.test();

setTimeout(()=>global.java.exit(), 1000);