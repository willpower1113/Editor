package com.willpower.jphoto.camera;

public interface JPhoto {

    boolean isSupportCamera2();

    /**
     * 拍照
     *
     * @param path
     */
    void photograph(String path);

    /**
     * 录像
     *
     * @param path
     */
    void recordVideo(String path);

    /**
     * 结束录像
     */
    void recordComplete();

    /**
     * 裁剪
     */
    void cut();

    /**
     * 压缩
     */
    void compress();
}
