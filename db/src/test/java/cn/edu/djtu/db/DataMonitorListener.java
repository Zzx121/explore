package cn.edu.djtu.db;

public interface DataMonitorListener {
    void exists(byte[] data);

    /**
     *
     * @param rc reason code
     */
    void closing(int rc);
}
