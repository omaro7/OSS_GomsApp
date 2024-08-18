package kr.co.goms.app.oss.manager.gson;

public interface GsonImpl {
    void builder();
    Object from(String data, Class gClass);
}
