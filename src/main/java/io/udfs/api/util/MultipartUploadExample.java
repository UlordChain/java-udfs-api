package io.udfs.api.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultipartUploadExample {
    public static void main(String[] args) throws Exception {
        //Creating CloseableHttpClient object
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpClient httpclient = HttpClients.custom()
                .addInterceptorFirst(new ContentLengthHeaderRemover()).setRedirectStrategy(new LaxRedirectStrategy())
                .build();

        //Creating a file object
        //String url="http://114.67.38.19:15001/api/v0/add";
        //String url="http://124.232.153.109:15001/api/v0/add";
        String url="http://api.udfs.one:15001/api/v0/delete/";
        //String filePath = "F:\\分布式存储\\宣传片\\少林寺BD国粤双语中字[电影天堂www.dy2018.com].mkv";
        String hash="QmWYJiC3itS1CNr5ys7c4eAr98f2D8q1PndgnRoGtJZpwC";

        /*String filePath1 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-0.mp4";
        String filePath2 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-1.mp4";
        String filePath3 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-2.mp4";
        String filePath4 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-3.mp4";
        String filePath5 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-4.mp4";
        String filePath6 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-5.mp4";
        String filePath7 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-6.mp4";
        String filePath8 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-7.mp4";
        String filePath9 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-8.mp4";
        String filePath10 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-9.mp4";
        String filePath11 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-10.mp4";
        String filePath12 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-11.mp4";
        String filePath13 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-12.mp4";
        String filePath14 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-13.mp4";
        String filePath15 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-14.mp4";
        String filePath16 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-15.mp4";
        String filePath17 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-16.mp4";
        String filePath18 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-17.mp4";
        String filePath19 = "F:\\分布式存储\\宣传片\\temp\\Ulord链云生态宣传片中英文字幕200M-18.mp4";*/
        //File file = new File(filePath);
        //UdfsDevTools tools = new UdfsDevTools(1000000, "76E18tAYEU2WPLww2DwPvM6", "udisk",filePath, "");
        //UdfsDevTools tools = new UdfsDevTools(1000000, "76E18tAYEU2WPLww2DwPvM6", "udisk",filePath, "");

        UdfsDevTools tools = new UdfsDevTools(1000, "D3BBEBC1E5004F429D301041B3771D9E", "bcdcentercom", "");

        //Creating the FileBody object
        //FileBody filebody = new FileBody(file, ContentType.DEFAULT_BINARY);

        //Creating the MultipartEntityBuilder
        MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();

        //Setting the mode
        entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        //Adding text
        //entitybuilder.addTextBody("sample_text", "This is the text part of our file");

        //Adding a file
        //entitybuilder.addBinaryBody("少林", new File(filePath));
        /*entitybuilder.addBinaryBody("宣传片2", new File(filePath2));
        entitybuilder.addBinaryBody("宣传片3", new File(filePath3));
        entitybuilder.addBinaryBody("宣传片4", new File(filePath4));
        entitybuilder.addBinaryBody("宣传片5", new File(filePath5));
        entitybuilder.addBinaryBody("宣传片6", new File(filePath6));
        entitybuilder.addBinaryBody("宣传片7", new File(filePath7));
        entitybuilder.addBinaryBody("宣传片8", new File(filePath8));
        entitybuilder.addBinaryBody("宣传片9", new File(filePath9));
        entitybuilder.addBinaryBody("宣传片10", new File(filePath10));
        entitybuilder.addBinaryBody("宣传片11", new File(filePath11));
        entitybuilder.addBinaryBody("宣传片12", new File(filePath12));
        entitybuilder.addBinaryBody("宣传片13", new File(filePath13));
        entitybuilder.addBinaryBody("宣传片14", new File(filePath14));
        entitybuilder.addBinaryBody("宣传片15", new File(filePath15));
        entitybuilder.addBinaryBody("宣传片16", new File(filePath16));
        entitybuilder.addBinaryBody("宣传片17", new File(filePath17));
        entitybuilder.addBinaryBody("宣传片18", new File(filePath18));*/

        //Building a single entity using the parts
        HttpEntity mutiPartHttpEntity = entitybuilder.build();

        //System.out.println(url+"?token=" + tools.getToken("add").toString());

        //Building the RequestBuilder request object
        RequestBuilder reqbuilder = RequestBuilder.post(url+hash+"?token=" + tools.getToken("get").toString());

        System.out.println(url+hash+"?token=" + tools.getToken("get").toString());


        //Set the entity object to the RequestBuilder
        reqbuilder.setEntity(mutiPartHttpEntity);

        //Building the request
        HttpUriRequest multipartRequest = reqbuilder.build();

        //Executing the request
        HttpResponse httpresponse = httpclient.execute(multipartRequest);

        //Printing the status and the contents of the response
        System.out.println(EntityUtils.toString(httpresponse.getEntity()));
        System.out.println(httpresponse.getStatusLine());
        //MultipartUploadExample.uploadFile();
    }

    public static int uploadFile() throws Exception {
        String url="http://test.api.udfs.one:15001/api/v0/add";
        String filePath = "F:\\分布式存储\\宣传片\\Ulord链云生态宣传片中英文字幕200M.mp4";
        UdfsDevTools tools = new UdfsDevTools(1000000, "test", "testaccountb",filePath, "");
        HttpURLConnection connection =
                (HttpURLConnection) (new URL(url+"?token=" + tools.getToken("add").toString())).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        FileBody fileBody = new FileBody(new File(filePath));
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
        multipartEntity.addPart("宣传片", fileBody);

        connection.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
        OutputStream out = connection.getOutputStream();
        try {
            multipartEntity.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
        int status = connection.getResponseCode();
        return status;

    }

}
