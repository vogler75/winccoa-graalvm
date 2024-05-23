package com.winccoa.test;

import com.winccoa.nodejs.DpElType;
import com.winccoa.nodejs.IWinccoa;
import com.winccoa.nodejs.WinccoaAsync;
import com.winccoa.nodejs.WinccoaCore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Test {

    public Test(IWinccoa scada) {
        switch (scada) {
            case WinccoaCore x -> testSequence(x);
            case WinccoaAsync x-> new Thread(()->testSequence(x)).start();
            default -> scada.logSevere("Unknown class type!");
        }
    }

    public void testSequence(IWinccoa scada) {
        scada.logInfo("Test Start");
        testBasics(scada);
        testDpNames(scada);
        testDpTypeCreate(scada);
        testDpSetTimed(scada);
        scada.logInfo("Test End.");
    }

    public void testBasics(IWinccoa scada) {
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
                .thenAccept((value)-> scada.logInfo("Set 0 Done!"));

        var promise1 = scada.dpSetWait("ExampleDP_Arg1.", 1)
                .thenAccept((value)-> scada.logInfo("Set 1 Done!"));

        var promise2 = scada.dpSetWait(List.of("ExampleDP_Arg1."), List.of(2))
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
    }

    public void testDpNames(IWinccoa scada) {
        scada.dpNames("Example*.**", "ExampleDP_Float", false).thenAccept((list)->{
            scada.logInfo("DpNames: "+String.join(",", list));
        });
        scada.dpNames("Example*.**", "", false).thenAccept((list)->{
            scada.logInfo("DpNames: "+String.join(",", list));
        });
    }

    public void testDpTypeCreate(IWinccoa scada) {
        String[][] elements = {
                {"MyType", ""},
                {"", "name"},
                {"", "speed"}
        };
        int[][] types = {
                {DpElType.STRUCT, 0},
                {0, DpElType.STRING},
                {0, DpElType.FLOAT}
        };
        scada.dpTypeCreate(elements, types).thenAccept((result)->{
            scada.logInfo("dpTypeCreate: "+result);
        });

        scada.dpCreate("MyType_Test_1", "MyType").thenAccept((result)->{
            scada.logInfo("dpCreate: "+result);
        });
    }

    public void testDpSetTimed(IWinccoa scada) {
        var t1 = new Date();
        var t2 = new Date();
        t2.setTime(t1.getTime()-10000);
        t2.setTime(t2.getTime() - t2.getTime() % 1000); // remove milliseconds
        scada.logInfo("Time1: "+t1+" Time2: "+t2);
        scada.dpSetTimed(t2, List.of("ExampleDP_Rpt3."), List.of(2))
                .thenAccept((value)-> scada.logInfo("dpSetTimed Done!"));
    }
}
