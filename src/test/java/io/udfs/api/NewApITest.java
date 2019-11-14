package io.udfs.api;
import io.udfs.api.util.UdfsDevTools;
import org.junit.Test;
import java.io.File;


public class NewApITest {

    @Test
    public void uploadFile() throws Exception{
        String url="http://test.api.udfs.one:15001/api/v0/add";
        //String filePath = "C:\\Users\\qupc\\Desktop\\111.txt";
        String type="add";
        String filePath="F:\\分布式存储\\UDFS上传文件素材\\微信图片_20181113205946.jpg";
        UdfsDevTools tools = new UdfsDevTools(1000, "test", "testaccountb", filePath, "");
        System.out.println(tools.getToken(type));
        File file = new File(filePath);
        String Result = UdfsDevTools.FilePost(url, file,tools.getToken(type));
    }
    @Test
    public void downloadFile() throws Exception{
        String hash="QmdQCd6hWaHwRT7fGJ4m7GTgWk3YdaEuotgP1McKKpQxWJ";
        String url="http://test.api.udfs.one:15001/api/v0/cat/"+hash;
        String type="get";
        UdfsDevTools tools = new UdfsDevTools(1000, "test", "testaccountb", "");
        System.out.println(tools.getToken(type));
        UdfsDevTools.download(url, "微信图片_20181113205946.jpg","F:\\分布式存储\\",tools.getToken(type));
        //System.out.println("下载结果:"+Result);
    }

}
