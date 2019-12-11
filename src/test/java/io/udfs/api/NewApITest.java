package io.udfs.api;
import io.udfs.api.util.UdfsDevTools;
import org.junit.Test;
import java.io.File;


public class NewApITest {

    @Test
    public void uploadFile() throws Exception{
        //String url="http://test.api.udfs.one:15001/api/v0/add";
        String filePath = "F:\\分布式存储\\宣传片\\少林寺BD国粤双语中字[电影天堂www.dy2018.com].mkv";
        String url="http://175.6.145.102:15001/api/v0/add";
        //String url="http://test.api.udfs.one:15001/api/v0/add";
        String type="add";
        //String filePath="C:\\Users\\Allen\\Desktop\\certificate\\1.png";
        //UdfsDevTools tools = new UdfsDevTools(100000, "D3BBEBC1E5004F429D301041B3771D9E", "bcdcentercom", filePath, "");
        UdfsDevTools tools = new UdfsDevTools(1000000, "testaccountb", "test",filePath, "");
        File file = new File(filePath);
        String Result = UdfsDevTools.FilePost(url, file,tools.getToken(type));
        //String Result = UdfsDevTools.FileUpload(url, filePath,tools.getToken(type));
        System.out.println("Result:"+Result);
    }
    @Test
    public void downloadFile() throws Exception{
        String hash="Qmc6harkPvfH1cz7m1mgAKtPAAqLSgA8AdUGwzyrbqCuaF";
        String url="http://api.udfs.one:15001/api/v0/cat/"+hash;
        String type="get";
        UdfsDevTools tools = new UdfsDevTools(1000000, "76E18tAYEU2WPLww2DwPvM6", "udisk", "");
        System.out.println(tools.getToken(type).toString());
        System.out.println(url += "?token=" + tools.getToken(type).toString());
        /*if(null!=tools){
            String tooken=tools.getToken(type);
            String result=UdfsDevTools.download(url, "1.mp4","F:\\分布式存储\\",tooken);
            System.out.println("下载结果:"+result);
        }*/
    }

    @Test
    public void downlLoadBytes() throws Exception{
        String hash="QmSes9eoCVtBAaVdTHRoB9FpGfYiFZtQ8359jmsZcWXfmo";
        String url="http://test.api.udfs.one:15001/api/v0/cat/"+hash;
        String type="get";
        UdfsDevTools tools = new UdfsDevTools(100, "test", "testaccountb", "");
        if(null!=tools){
            String token=tools.getToken(type);
            byte[] bytes =UdfsDevTools.downlLoadBytes(url, "微信图片_20181113205946.jpg","F:\\分布式存储\\",token);
            System.out.println("输出字符数组"+bytes);
        }
    }

    @Test
    public void deleteFile() throws Exception{
        String hash="QmWYJiC3itS1CNr5ys7c4eAr98f2D8q1PndgnRoGtJZpwC";
        String url="http://api.udfs.one:15001/api/v0/delete/"+hash;
        String type="get";
        UdfsDevTools tools = new UdfsDevTools(1000, "D3BBEBC1E5004F429D301041B3771D9E", "bcdcentercom", "");
        if(null!=tools){
            String token=tools.getToken(type);
            int result=UdfsDevTools.DelFile(url, token);
            System.out.println("删除结果:"+result);
        }
    }

}
