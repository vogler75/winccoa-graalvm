const { WinccoaConnectUpdateType } = require('winccoa-manager');

// -----------------------------------------------------------------------------------------------------------------

function dpConnect(uuid, dps, answer) {
    function dpConnectCallback(names, values, type, error) {
        if (error) {
            console.log("Node::dpConnectCallback Error: "+uuid+" "+error);
        } else {
            let answer = (type == WinccoaConnectUpdateType.Answer);
            java.dpConnectCallback(uuid, names, values, answer);
        }
    }
    try {
        if (Array.isArray(dps)) {
            dps = dps.map(item => String(item));
        }
        id = scada.dpConnect(dpConnectCallback, dps, answer);
        console.log("Node::dpConnect("+uuid+","+dps+","+id+")");
        return id;
    } catch (e) {
        console.error(e);
        return -1;
    }
}

function dpDisconnect(id) {
    try {
        ret = scada.dpDisconnect(id);
        console.log("Node::dpDisconnect("+id+","+ret+")");
        return ret;
    } catch (e) {
        console.error(e);
        return -1;
    }
}

// -----------------------------------------------------------------------------------------------------------------

function dpQueryConnectSingle(uuid, query, answer) {
    function dpQueryConnectSingleCallback(values, type, error) {
        if (error) {
            console.log("Node::dpQueryConnectSingleCallback Error: "+uuid+" "+error);
        } else {
            let answer = (type == WinccoaConnectUpdateType.Answer);
            java.dpQueryConnectCallback(uuid, values, answer);
        }
    }
    try {
        id = scada.dpQueryConnectSingle(dpQueryConnectSingleCallback, answer, query);
        console.log("Node::dpQueryConnectSingle("+uuid+","+query+","+id+")");
        return id;
    } catch (e) {
        console.error(e);
        return -1;
    }
}

function dpQueryDisconnect(id) {
    try {
        ret = scada.dpQueryDisconnect(id);
        console.log("Node::dpQueryDisconnect("+id+","+ret+")");
        return ret;
    } catch (e) {
        console.error(e);
        return -1;
    }
}

// -----------------------------------------------------------------------------------------------------------------

module.exports = {
    dpConnect,
    dpDisconnect,
    dpQueryConnectSingle,
    dpQueryDisconnect
};