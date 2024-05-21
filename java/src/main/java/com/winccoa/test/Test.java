package com.winccoa.test;

import com.winccoa.nodejs.IWinccoa;
import com.winccoa.nodejs.WinccoaAsync;
import com.winccoa.nodejs.WinccoaCore;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Test {

    public Test(IWinccoa scada) {
        switch (scada) {
            case WinccoaCore x -> test1(x);
            case WinccoaAsync x-> new Thread(()->test1(x)).start();
            default -> scada.logSevere("Unknown class type!");
        }
    }

    public void test1(IWinccoa scada) {
        scada.logInfo("Test Start");

        var id1 = UUID.randomUUID().toString();
        scada.dpConnect(id1, "ExampleDP_Rpt1.", true, (data) -> {
            scada.logInfo("Callback Single "+ Arrays.toString(data.names()) +" "+ Arrays.toString(data.values()));
        });

        var id2 = UUID.randomUUID().toString();
        scada.dpConnect(id2, Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2."), true, (data) -> {
            scada.logInfo("Callback Array "+ Arrays.toString(data.names()) +" "+ Arrays.toString(data.values()));
        });

        var id3 = UUID.randomUUID().toString();
        var sql = "SELECT '_online.._value' FROM '*' WHERE _DPT= \"ExampleDP_Float\"";
        scada.dpQueryConnectSingle(id3, sql, true, (data) -> {
            scada.logInfo("Callback Query: "+data.values().length);
            List.of(data.values()).forEach((row)-> {
                scada.logInfo("+ "+Arrays.toString(row));
            });
        });

        var promise0 = scada.dpSet("ExampleDP_Arg1.", 0)
                .thenAccept((value)-> scada.logInfo("Set 0 Doen!"));

        var promise1 = scada.dpSetWait("ExampleDP_Arg1.", 1)
                .thenAccept((value)-> scada.logInfo("Set 1 Done!"));

        var promise2 = scada.dpSetWait("ExampleDP_Arg1.", 2)
                .thenAccept((value)-> scada.logInfo("Set 2 Done!"));

        var promise3 = scada.dpSetWait(
                Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2."),
                Arrays.asList(3,3)
        ).thenAccept((value)-> scada.logInfo("Set 3 Done!"));

        CompletableFuture.allOf(promise0, promise1, promise2, promise3).thenAccept((unused)-> {
            scada.logInfo("Disconnect: "+scada.dpDisconnect(id1));
            scada.logInfo("Disconnect: "+scada.dpDisconnect(id2));
            scada.logInfo("Disconnect: "+scada.dpQueryDisconnect(id3));
            scada.dpGet("ExampleDP_Arg1.")
                    .thenAccept((value)->scada.logInfo("dpGet: "+value.toString()));
            scada.dpGet(Arrays.asList("ExampleDP_Arg1.", "ExampleDP_Arg2."))
                    .thenAccept((value)->scada.logInfo("dpGet: "+value.toString()));
        });

        scada.dpSet(Arrays.asList("ExampleDP_Rpt1.","ExampleDP_Rpt2."), Arrays.asList(3, 4))
                .thenAccept((value)->scada.logInfo("Set Array "+value));

        scada.logInfo("Test End.");
    }
}
