const { WinccoaConnectUpdateType } = require('winccoa-manager');

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

module.exports = {
    dpConnect,
    dpDisconnect
};