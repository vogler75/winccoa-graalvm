'use strict';

// require WinCC OA interface
const { WinccoaManager, WinccoaSecurityEventId, WinccoaConnectUpdateType } = require('winccoa-manager');
global.scada = new WinccoaManager();

// Node.js communication to Java
global.node = require('./node');

// Main Java object for communication
global.java = new (Java.type('com.winccoa.nodejs.WinccoaAsync'))();
const intervalId = setInterval(() => { while (java.loop()); }, 1);

global.java.test();