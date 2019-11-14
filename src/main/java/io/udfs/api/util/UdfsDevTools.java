package io.udfs.api.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import sun.misc.IOUtils;
import sun.swing.StringUIClientPropertyKey;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

public class UdfsDevTools {

    private long expireSec;             //当前时间后多少秒过期
    private String udfsCallbackUrl;     //udfs回调函数
    private String secretKey;           //udfs秘钥，开发者向Ulord基金会申请
    private String uosAccount;          //UOS账号，可以向U1号注册
    private String filePath;

    public UdfsDevTools(long expiresec, String secretkey, String uosaccount, String filepath, String udfscallbackurl) throws UnsupportedEncodingException {
        expireSec = expiresec;
        secretKey = secretkey;
        uosAccount = uosaccount;
        filePath = filepath;
        udfsCallbackUrl = udfscallbackurl;
    }

    public UdfsDevTools(long expiresec, String secretkey, String uosaccount, String udfscallbackurl) throws UnsupportedEncodingException {
        expireSec = expiresec;
        secretKey = secretkey;
        uosAccount = uosaccount;
        udfsCallbackUrl = udfscallbackurl;
    }

    public String getToken(String type) throws Exception {
        long expireTime = System.currentTimeMillis() / 1000L;
        expireTime += expireSec;
        String json = "";
        if(type.equals("add")){
            File f = new File(filePath);
            String fileName = f.getName();
            InputStream fis = new FileInputStream(f);
            int fileSize = fis.available();
            fis.close();
            String md5 = getMD5Three(filePath);
            if (StringUtils.isEmpty(udfsCallbackUrl))
                json = "{\"ver\":0,\"expired\":" + expireTime + ",\"callback_url\":\"\",\"callback_body\":\"\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"md5\":\"" + md5 + "\"}}";
            else
                json = "{\"ver\":0,\"expired\":" + expireTime + ",\"callback_url\":\"" + udfsCallbackUrl + "\",\"callback_body\":\"{\\\"file_size\\\":\\\"$(size)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":0}\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"md5\":\"" + md5 + "\"}}";

        }else if (type.equals("get")){
                json = "{\"ver\":0,\"expired\": "+expireTime+",\"ext\":{}}";
        }
        byte[] textByte;
        String encodedPolicy;
        byte[] sign;
        Base64.Encoder encoder = Base64.getEncoder();
        try {
            textByte = json.getBytes("UTF-8");
            encodedPolicy = UrlSafeBase64.encodeToString(textByte);
            sign = HMAC_SHA1.genHMAC(encodedPolicy, secretKey);
        } catch (Exception ex) {
            throw new Exception("构建下载链接出错" + ex.toString());
        }
        String encodeSign = UrlSafeBase64.encodeToString(sign);
        String token = uosAccount + ":" + encodeSign + ":" + encodedPolicy;
        return token;
    }

    public static String getMD5Three(String path) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(path);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }

    /**
     * 上传文件
     * @param url
     * @param value
     * @param token
     * @return
     * @throws Exception
     */
    public static String FilePost(String url,File value,String token) throws Exception {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "--------------------" + BOUNDARY, Charset.defaultCharset());
        multipartEntity.addPart("binary", new FileBody(value));
        url+="?token="+token;
        HttpPost request = new HttpPost(url);
        request.setEntity(multipartEntity);
        request.addHeader("Content-Type", "multipart/form-data; boundary=--------------------" + BOUNDARY);
        HttpClient client = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
        HttpResponse response = client.execute(request);
        InputStream is = response.getEntity().getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        System.out.println("发送消息收到的返回：" + buffer.toString());
        return buffer.toString();
    }

    /**
     * 下载文件
     * @param urlString
     * @param filename
     * @param savePath
     * @param token
     * @throws Exception
     */
    public static void download(String urlString, String filename,String savePath,String token) throws Exception {

        urlString+="?token="+token;
        // 构造URL
        System.out.println(urlString);
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        //设置请求超时为5s
        con.setConnectTimeout(5*1000);
        // 输入流
        InputStream is = con.getInputStream();

        // 1K的数据缓冲
        byte[] bs = new byte[4096];
        // 读取到的数据长度
        int len;
        // 输出的文件流
        File sf=new File(savePath);
        if(!sf.exists()){
            sf.mkdirs();
        }
        OutputStream os = new FileOutputStream(sf.getPath()+"\\"+filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
    public static void main(String[] args) throws Exception {

        //UdfsDevTools tools = new UdfsDevTools(1000, "test", "testaccountb", "C:\\Users\\qupc\\Desktop\\ips.txt", "");
        //UdfsDevTools tools = new UdfsDevTools(1000, "test", "testaccountb", "C:\\Users\\Allen\\Desktop\\20191102-new.txt", "");
        //C:\Users\Allen\Desktop\20191102-new.txt
        //String token = tools.getToken();
        //System.out.println(token);
    }
}
