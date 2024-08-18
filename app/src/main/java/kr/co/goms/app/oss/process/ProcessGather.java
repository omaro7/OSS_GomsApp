package kr.co.goms.app.oss.process;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;

public class ProcessGather {
    private final static String LOG_TAG = ProcessGather.class.getSimpleName();

    private Handler handler = null;

    private HashMap<Integer, Object> processHashMap = null;

    private Complete complete = null;

    public void create() {
        Log.d(LOG_TAG, "create()");
        if(null == handler) {
//            handler = new Handler(new Handler.Callback() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
//                public boolean handleMessage(Message msg) {
                public void handleMessage(Message msg) {

                    int pid = msg.what;
                    Object object = onRemove(pid);
                    boolean completeFlag;
                    int remainProcess = processHashMap.size();

                    if(0 < remainProcess) {
                        // 아직 완료 되지 않음
                        Log.d(LOG_TAG, "Remain Process : " + remainProcess);
                        completeFlag = false;
                    } else {
                        // 완료 됨 - 모든 프로세스가 제거되는 순간이 완료.
                        Log.d(LOG_TAG, "Remain Process : " + remainProcess);
                        completeFlag = true;
                    }

                    if(null != complete) {
                        complete.complete(completeFlag, pid, object, remainProcess);
                    }

//                    return true;
                }
            };
        }

        if(null == processHashMap) {
            processHashMap = new HashMap<Integer, Object>();
        }
    }

    /**
     * 프로세스 생성
     * @return 생성된 Process Identifier
     */
    public int createProcess(Object value) {
        Log.d(LOG_TAG, "createProcess()");

        UUID uuid = UUID.randomUUID();
        int pid = uuid.hashCode();

        int loopCount = 10;

        Object obj;

        while(0 < loopCount) {
            obj = processHashMap.get(pid);
            if(null != obj) {
                uuid = UUID.randomUUID();
                pid = uuid.hashCode();
                --loopCount;
                Log.d(LOG_TAG, "createProcess PID - Overlap, Reissue");
            } else {
                loopCount = 0;
                Log.d(LOG_TAG, "createProcess PID - Now Issue");
            }
        }

        Log.d(LOG_TAG, "createProcess PID : " + pid);
        processHashMap.put(pid, value);

        return pid;
    }

    /**
     * 프로세스 종료 알림
     * @param pid 종료할 Process Identifier
     */
    public void finishProcess(int pid) {
        handler.sendEmptyMessage(pid);
        Log.d(LOG_TAG, "finishProcess : " + pid);
    }

    /**
     * 프로세스 제거
     * @param pid 제거할 Process Identifier
     * @return 제거 성공
     */
    private Object onRemove(int pid) {
        if(null == processHashMap) {
            return null;
        }

        Log.d(LOG_TAG, "onRemove : " + pid);

        return processHashMap.remove(pid);
    }

    /**
     * 리스너 등록
     * @param completeListener 응답 받을 리스너 등록
     */
    public void setOnCompleteListener(Complete completeListener) {
        complete = completeListener;
    }

    /**
     * 완료 알림 인터페이스
     */
    public interface Complete {
        /**
         * @param flag true : 완료, false : 미완료
         * @param pid 현재 종료된 프로세트의 pid
         * @param remainProcess 현재 남은 프로세스 수
         */
        void complete(boolean flag, int pid, Object object, int remainProcess);
    }
}
