'use strict';

/*
    Don't change the names of scada, node, java!
    Those JavaSccript objects are referenced in the Java code!
    If you change those names it won't work anymore!
*/

// require WinCC OA interface
const { WinccoaManager } = require('winccoa-manager');
global.scada = new WinccoaManager();

// Node.js communication to Java
global.node = require('./node');

// Main Java object for communication
global.java = new (Java.type('com.winccoa.nodejs.WinccoaCore'))();

// Test object
global.test = new (Java.type('com.winccoa.test.Test'))(java);

setTimeout(()=>global.java.exit(), 10000);