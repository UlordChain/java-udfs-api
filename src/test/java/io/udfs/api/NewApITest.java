package io.udfs.api;
import io.udfs.api.util.UdfsDevTools;
import org.junit.Test;
import java.io.File;


public class NewApITest {

    @Test
    public void uploadFile() throws Exception{
        //String url="http://test.api.udfs.one:15001/api/v0/add";
        String filePath = "C:\\Users\\Allen\\Desktop\\certificate\\1.png";
        String url="http://api.udfs.one:15001/api/v0/add";
        String type="add";
        //String filePath="C:\\Users\\Allen\\Desktop\\certificate\\1.png";
        UdfsDevTools tools = new UdfsDevTools(1000, "", "", filePath, "");
        File file = new File(filePath);
        String Result = UdfsDevTools.FilePost(url, file,tools.getToken(type));
        System.out.println("Result:"+Result);
    }
    @Test
    public void downloadFile() throws Exception{
        String hash="";
        String url="http://test.api.udfs.one:15001/api/v0/cat/"+hash;
        String type="get";
        UdfsDevTools tools = new UdfsDevTools(100, "", "", "");
        System.out.println(tools.getToken(type));
        UdfsDevTools.download(url, "微信图片_20181113205946.jpg","F:\\分布式存储\\",tools.getToken(type));
        //System.out.println("下载结果:"+Result);
    }

}
