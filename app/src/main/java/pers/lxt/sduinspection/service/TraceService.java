package pers.lxt.sduinspection.service;

import android.content.Context;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;

import pers.lxt.sduinspection.model.Task;

public class TraceService {

    private static final long SERVICE_ID = 164668;

    private static TraceService _traceService;
    public static TraceService getInstance(Context context){
        if(_traceService == null){
            _traceService = new TraceService(context);
        }
        return _traceService;
    }

    private LBSTraceClient mTraceClient;
    private Trace mTrace;
    private int traceCount;

    private TraceService(Context context){
        traceCount = 0;
        mTraceClient = new LBSTraceClient(context);
        mTraceClient.setInterval(5, 10);
        mTrace = new Trace(SERVICE_ID, TokenService.getInstance(context).getPhone(), false);
    }

    public void startTrace(){
        if(traceCount == 0){
            mTraceClient.startTrace(mTrace, new OnTraceListener() {
                @Override
                public void onBindServiceCallback(int i, String s) {

                }

                @Override
                public void onStartTraceCallback(int i, String s) {
                    mTraceClient.startGather(null);
                }

                @Override
                public void onStopTraceCallback(int i, String s) {

                }

                @Override
                public void onStartGatherCallback(int i, String s) {

                }

                @Override
                public void onStopGatherCallback(int i, String s) {

                }

                @Override
                public void onPushCallback(byte b, PushMessage pushMessage) {

                }

                @Override
                public void onInitBOSCallback(int i, String s) {

                }
            });
        }
        traceCount++;
    }

    public void stopTrace(){
        if(traceCount == 0) return;
        mTraceClient.stopTrace(mTrace, null);
        traceCount--;
    }

    public boolean getTrace(Task task, OnTrackListener listener){
        if(task.getStartTime() == null) return false;
        HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(1, SERVICE_ID, task.getAssignee());
        historyTrackRequest.setStartTime(task.getStartTime().getTime() / 1000);
        if(task.getEndTime() == null){
            historyTrackRequest.setEndTime(System.currentTimeMillis() / 1000);
        }else{
            historyTrackRequest.setEndTime(task.getEndTime().getTime() / 1000);
        }
        mTraceClient.queryHistoryTrack(historyTrackRequest, listener);
        return true;
    }
}
