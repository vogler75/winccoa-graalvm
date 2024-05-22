const { WinccoaConnectUpdateType, WinccoaCtrlType, WinccoaCtrlScript } = require('winccoa-manager');

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

function dpTypeCreate(elements, types) {
    const script = new WinccoaCtrlScript(
        scada,
        `int main(dyn_dyn_string elements, dyn_dyn_int types)
         {
           return dpTypeCreate(elements, types); 
         }`
    );
    elements = elements.map(item => item.map(item => String(item)));
    types = types.map(item => item.map(item => Number(item)));
    return script.start( // returns a promise
        'main',
        [elements, types],
        [WinccoaCtrlType.dyn_dyn_string, WinccoaCtrlType.dyn_dyn_int]
    );   
}

function dpTypeDelete(dpt) {
    const script = new WinccoaCtrlScript(
        scada,
        `int main(string dpt)
         {
           return dpTypeDelete(dpt); 
         }`
    );
    return script.start( // returns a promise
        'main',
        [dpt],
        [WinccoaCtrlType.string]
    );   
}

// -----------------------------------------------------------------------------------------------------------------

module.exports = {
    dpConnect,
    dpDisconnect,
    dpQueryConnectSingle,
    dpQueryDisconnect,
    dpTypeCreate,
    dpTypeDelete
};