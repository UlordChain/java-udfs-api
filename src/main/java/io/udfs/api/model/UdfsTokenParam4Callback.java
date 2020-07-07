package io.udfs.api.model;

public class UdfsTokenParam4Callback {
    private String ver="0";
    private Long expired;
    private String callback_url;
    private String callback_body;
    private ext ext;

    public UdfsTokenParam4Callback(Long expireTime,String callBackUrl,String id,String extend,String file_name,Integer size,String md5) {
        ext = new ext();
        ext.setFile_name(file_name);
        ext.setSize(size);
        ext.setMd5(md5);
        expired = expireTime;
        callback_url = callBackUrl;
        callback_body = "{\\\"file_size\\\":\\\"$(size)\\\",\\\"file_name\\\":\\\"$(file_name)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":\\\"" + id + "\\\",\\\"extend\\\":\\\"" + extend + "\\\"}";
    }



    public String getVer() {
        return ver;
    }

    public Long getExpired() {
        return expired;
    }

    public io.udfs.api.model.ext getExt() {
        return ext;
    }


    public String getCallback_url() {
        return callback_url;
    }

    public String getCallback_body() {
        return callback_body;
    }
}
