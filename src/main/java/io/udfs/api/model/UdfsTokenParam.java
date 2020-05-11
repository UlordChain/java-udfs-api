package io.udfs.api.model;


public class UdfsTokenParam {

    private String ver="0";
    private Long expired;
    private ext ext;

    public UdfsTokenParam(Long expireTime,String file_name,Integer size,String md5)
    {
        ext =new ext();
        ext.setFile_name(file_name);
        ext.setSize(size);
        ext.setMd5(md5);
        expired=expireTime;
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
}
