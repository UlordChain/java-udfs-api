package io.udfs.api.util;

import io.udfs.api.model.UdfsTokenParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UdfsDevTools {
    private long expireSec;             //当前时间后多少秒过期
    private String udfsCallbackUrl;     //udfs回调函数
    private String secretKey;           //udfs秘钥，开发者向Ulord基金会申请
    private String uosAccount;          //UOS账号，可以向U1号注册
    private String filePath;
    private String fileId;

    private String md5;
    private String fileName;
    private Long fileSize;

    public UdfsDevTools(long expiresec, String secretkey, String uosaccount,String file_id, String filepath, String udfscallbackurl){
        expireSec = expiresec;
        secretKey = secretkey;
        uosAccount = uosaccount;
        filePath = filepath;
        fileId= file_id;
        udfsCallbackUrl = udfscallbackurl;
    }

    public UdfsDevTools(long expiresec, String secretkey, String uosaccount, String file_id , String udfscallbackurl){
        expireSec = expiresec;
        secretKey = secretkey;
        uosAccount = uosaccount;
        fileId= file_id;
        udfsCallbackUrl = udfscallbackurl;
    }

    public UdfsDevTools(long expiresec, String secretkey, String uosaccount, String udfscallbackurl){
        expireSec = expiresec;
        secretKey = secretkey;
        uosAccount = uosaccount;
        udfsCallbackUrl = udfscallbackurl;
    }

    /**
     * 根据不同类型获取用户token数据
     * @param type
     * @return
     * @throws Exception
     */
    public String getToken(String type) throws Exception {
        String token="";
        if(expireSec==0L||StringUtils.isEmpty(secretKey)||StringUtils.isEmpty(uosAccount)){
            return token;
        }else {
            long expireTime = System.currentTimeMillis() / 1000L;
            expireTime += expireSec;
            String json = "";
            if(type.equals("add")) {
                File f = new File(filePath);
                fileName = f.getName();
                InputStream fis = new FileInputStream(f);
                fileSize = Long.valueOf(fis.available());
                fis.close();
                md5 = DigestUtils.md5Hex(new FileInputStream(filePath));

                if (StringUtils.isEmpty(udfsCallbackUrl))
                    json = "{\"ver\":0,\"expired\":" + expireTime + ",\"callback_url\":\"\",\"callback_body\":\"\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"md5\":\"" + md5 + "\"}}";
                else
                    json = "{\"ver\":0,\"expired\":" + expireTime + ",\"callback_url\":\"" + udfsCallbackUrl + "\",\"callback_body\":\"{\\\"file_size\\\":\\\"$(size)\\\",\\\"file_name\\\":\\\"$(file_name)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":\\\"" + fileId + "\\\"}\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"md5\":\"" + md5 + "\"}}";

            }else if (type.equals("get")){
                json = "{\"ver\":0,\"expired\": "+expireTime+",\"ext\":{}}";
            }
            byte[] textByte;
            String encodedPolicy;
            byte[] sign;
            //Base64.Encoder encoder = Base64.getEncoder();
            try {
                if(StringUtils.isNotEmpty(json)){
                    textByte = json.getBytes("UTF-8");
                    encodedPolicy = UrlSafeBase64.encodeToString(textByte);
                    sign = HMAC_SHA1.genHMAC(encodedPolicy, secretKey);
                    String encodeSign = UrlSafeBase64.encodeToString(sign);
                    token = uosAccount + ":" + encodeSign + ":" + encodedPolicy;
                }
            } catch (Exception ex) {
                throw new Exception("构建下载链接出错" + ex.toString());
            }
            return token;
        }
    }

    /**
     * 根据不同类型获取用户token数据
     * @param type
     * @return
     * @throws Exception
     */
    public String getToken(String type,String encry,String hash) throws Exception {
        String token="";
        if(StringUtils.isEmpty(type)){
            return token;
        }else {
            long expireTime = System.currentTimeMillis() / 1000L;
            expireTime += expireSec;
            String json = "";
            if(type.equals("add")){
                File f = new File(filePath);
                String fileName = f.getName();
                InputStream fis = new FileInputStream(f);
                int fileSize = fis.available();
                fis.close();
                if(StringUtils.isNotEmpty(encry)&&encry.equals("md5")){
                    String md5= DigestUtils.md5Hex(new FileInputStream(filePath));
                    if (StringUtils.isEmpty(udfsCallbackUrl))
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"\",\"callback_body\":\"\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":0,\"check_sum\":\"" + md5 + "\"}}";
                    else
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"" + udfsCallbackUrl + "\",\"callback_body\":\"{\\\"file_size\\\":\\\"$(size)\\\",\\\"file_name\\\":\\\"$(file_name)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":\\\""+fileId+"\\\"}\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":0,\"check_sum\":\"" + md5 + "\"}}";
                }else if(StringUtils.isNotEmpty(encry)&&encry.equals("sha256")){
                    String sha256= DigestUtils.sha256Hex(new FileInputStream(filePath));
                    if (StringUtils.isEmpty(udfsCallbackUrl))
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"\",\"callback_body\":\"\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":1,\"check_sum\":\"" + sha256 + "\"}}";
                    else
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"" + udfsCallbackUrl + "\",\"callback_body\":\"{\\\"file_size\\\":\\\"$(size)\\\",\\\"file_name\\\":\\\"$(file_name)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":\\\""+fileId+"\\\"}\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":1,\"check_sum\":\"" + sha256 + "\"}}";

                }

            }else if (type.equals("get")){
                if(StringUtils.isNotEmpty(hash)){
                    json = "{\"ver\":1,\"expired\": "+expireTime+",\"ext\":{\"hash\":\"" + hash + "\"}}";
                }
            }
            byte[] textByte;
            String encodedPolicy;
            byte[] sign;
            //Base64.Encoder encoder = Base64.getEncoder();
            try {
                if(StringUtils.isNotEmpty(json)){
                    textByte = json.getBytes("UTF-8");
                    encodedPolicy = UrlSafeBase64.encodeToString(textByte);
                    sign = HMAC_SHA1.genHMAC(encodedPolicy, secretKey);
                    String encodeSign = UrlSafeBase64.encodeToString(sign);
                    token = uosAccount + ":" + encodeSign + ":" + encodedPolicy;
                }
            } catch (Exception ex) {
                throw new Exception("构建下载链接出错" + ex.toString());
            }
            return token;
        }
    }

    /**
     * 根据已有的值生成token
     * @param type  add/get，上传用add，下载用get
     * @param encry md5/sha256
     * @param value md5或者sha256的值
     * @param fileName  文件名称
     * @param fileSize  文件大小
     * @param hash  get方法时需要传文件的hash值，其他方法为空
     * @return
     */
    public String getEncryToken(String type,String encry,String value,String fileName,long fileSize,String hash) throws Exception {
        String token="";
        if(StringUtils.isEmpty(type)){
            return token;
        }else {
            long expireTime = System.currentTimeMillis() / 1000L;
            expireTime += expireSec;
            String json = "";
            if(type.equals("add")&&StringUtils.isNotEmpty(fileName)&&fileSize!=0L){
                if(StringUtils.isNotEmpty(encry)&&encry.equals("md5")){
                    if (StringUtils.isEmpty(udfsCallbackUrl))
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"\",\"callback_body\":\"\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":0,\"check_sum\":\"" + value + "\"}}";
                    else
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"" + udfsCallbackUrl + "\",\"callback_body\":\"{\\\"file_size\\\":\\\"$(size)\\\",\\\"file_name\\\":\\\"$(file_name)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":\\\""+fileId+"\\\"}\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":0,\"check_sum\":\"" + value+ "\"}}";
                }else if(StringUtils.isNotEmpty(encry)&&encry.equals("sha256")){
                    if (StringUtils.isEmpty(udfsCallbackUrl))
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"\",\"callback_body\":\"\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":1,\"check_sum\":\"" + value + "\"}}";
                    else
                        json = "{\"ver\":1,\"expired\":" + expireTime + ",\"callback_url\":\"" + udfsCallbackUrl + "\",\"callback_body\":\"{\\\"file_size\\\":\\\"$(size)\\\",\\\"file_name\\\":\\\"$(file_name)\\\",\\\"hash\\\":\\\"$(hash)\\\",\\\"file_id\\\":\\\""+fileId+"\\\"}\",\"ext\":{\"file_name\":\"" + fileName + "\",\"size\":" + fileSize + ",\"verify_type\":1,\"check_sum\":\"" + value+ "\"}}";

                }

            }else if (type.equals("get")){
                if(StringUtils.isNotEmpty(hash)){
                    json = "{\"ver\":1,\"expired\": "+expireTime+",\"ext\":{\"hash\":\"" + hash + "\"}}";
                }
            }
            byte[] textByte;
            String encodedPolicy;
            byte[] sign;
            if(StringUtils.isNotEmpty(json)){
                textByte = json.getBytes("UTF-8");
                encodedPolicy = UrlSafeBase64.encodeToString(textByte);
                sign = HMAC_SHA1.genHMAC(encodedPolicy, secretKey);
                String encodeSign = UrlSafeBase64.encodeToString(sign);
                token = uosAccount + ":" + encodeSign + ":" + encodedPolicy;
                }
            return token;
        }
    }


    /**
     * 上传文件
     * @param url
     * @param value
     * @param token
     * @return
     * @throws IOException
     */

    public static String FilePost(String url,File value,String token) throws Exception{
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(value.getPath()) || StringUtils.isEmpty(token)) {
            return null;
        } else {
            //默认信任证书
           /* SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpclient = HttpClients.custom().disableAutomaticRetries()
                    .setRedirectStrategy(new LaxRedirectStrategy()).setSSLSocketFactory(sslConnectionSocketFactory)
                    .build();*/
            CloseableHttpClient httpclient = HttpClients.custom().disableAutomaticRetries()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            try {
                HttpClientContext context = HttpClientContext.create();
                HttpPost httpPost = new HttpPost(url + "?token=" + token);
                System.out.println("Executing request " + httpPost.getRequestLine());
                System.out.println("----------------------------------------");
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
                multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式
                multipartEntityBuilder.addBinaryBody("file", value);
                httpPost.setEntity(multipartEntityBuilder.build());
                CloseableHttpResponse response=httpclient.execute(httpPost, context);
                String result=EntityUtils.toString(response.getEntity());
                HttpHost target = context.getTargetHost();
                List<URI> redirectLocations = context.getRedirectLocations();
                URI location = URIUtils.resolve(httpPost.getURI(), target, redirectLocations);
                System.out.println("Final HTTP location: " + location.toASCIIString());
                return result;

            }catch (Exception e){
                System.out.println("错误信息为："+e.getMessage());
                return null;
            }finally {
                httpclient.close();
            }
        }
    }

    /**
     * 上传文件
     * @param url
     * @param value
     * @return
     * @throws IOException
     */

    public static String NewFilePost(String url,File value) throws IOException {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(value.getPath())) {
            return null;
        } else {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpClientContext context = HttpClientContext.create();
                HttpPost httpPost = new HttpPost(url);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
                multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式
                multipartEntityBuilder.addBinaryBody("file", value);
                httpPost.setEntity(multipartEntityBuilder.build());
                CloseableHttpResponse response=httpclient.execute(httpPost, context);
                String result=EntityUtils.toString(response.getEntity(),"UTF-8");
                return result;

            }catch (Exception e){
                System.out.println("错误信息为："+e.getMessage());
                return null;
            }finally {
                httpclient.close();
            }
        }
    }


    /**
     * 上传文件
     * @param url
     * @param token
     * @return
     * @throws IOException
     */

    public static String newFilePost(String url,File file,String token) throws IOException {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(file.getPath()) || StringUtils.isEmpty(token)) {
            return null;
        } else {
            CloseableHttpClient httpClient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            HttpPost httpPost = new HttpPost(url + "?token=" + token);
            //DecimalFormat df = new DecimalFormat("#.##");
            String suffix="mkv";
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            /*builder.addTextBody("name", file.getOriginalFilename(), ContentType.create("text/plain", Consts.UTF_8));
            builder.addTextBody("size", 1048303874 / 1024), ContentType.TEXT_PLAIN);
            builder.addTextBody("suffix", suffix, ContentType.TEXT_PLAIN);*/
            //要上传的文件
            builder.addBinaryBody("file", file);
            httpPost.setEntity(builder.build());
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result=EntityUtils.toString(response.getEntity());
            return result;
        }
    }

    /**
     * 删除文件
     * @param url
     * @param token
     * @return
     * @throws Exception
     */
    public static int DelFile(String url,String token) throws Exception {
        if(StringUtils.isEmpty(url)||StringUtils.isEmpty(token)){
            return -1;
        }else {
            CloseableHttpClient httpclient = HttpClients.custom()
                    .addInterceptorFirst(new ContentLengthHeaderRemover()).setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
            entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            HttpEntity mutiPartHttpEntity = entitybuilder.build();
            RequestBuilder reqbuilder = RequestBuilder.post(url+"?token="+token);
            System.out.println("url:"+url+"?token="+token);
            reqbuilder.setEntity(mutiPartHttpEntity);
            HttpUriRequest multipartRequest = reqbuilder.build();
            HttpResponse httpresponse = httpclient.execute(multipartRequest);
            System.out.println(EntityUtils.toString(httpresponse.getEntity()));
            return httpresponse.getStatusLine().getStatusCode();
        }
    }


    /**
     * 下载文件
     * @param urlString
     * @param filename
     * @param savePath
     * @param token
     * @throws Exception
     */
    public static String download(String urlString, String filename,String savePath,String token) throws Exception {
        String result;
        if(StringUtils.isEmpty(urlString)||StringUtils.isEmpty(filename)||StringUtils.isEmpty(token)){
            result= "error";

        }else {
            InputStream is=null;
            OutputStream os=null;
            try{
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
                is = con.getInputStream();
                // 1K的数据缓冲
                byte[] bs = new byte[4096];
                // 读取到的数据长度
                int len;
                // 输出的文件流
                File sf=new File(savePath);
                if(!sf.exists()){
                    sf.mkdirs();
                }
                os = new FileOutputStream(sf.getPath()+"\\"+filename);
                // 开始读取
                while ((len = is.read(bs)) != -1) {
                    os.write(bs, 0, len);
                }
                result="success";
            }catch (Exception e){
                System.out.println(e.getMessage());
                result="error";

            }finally {
                // 完毕，关闭所有链接
                if(null!=os&&null!=is){
                    os.close();
                    is.close();
                }
            }

        }
        return result;
    }

    /**
     * 下载文件
     */
    public static void downloadFile(String urlString, String filename,String savePath,String token) throws Exception{
        CloseableHttpClient httpClient = HttpClients.custom().build();
        CloseableHttpResponse response = httpClient.execute(new HttpGet("https://www.example.com"));
        InputStream is = response.getEntity().getContent();
        Files.copy(is, new File("temp.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
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

    public static byte[] downlLoadBytes(String urlString, String filename,String savePath,String token) throws Exception {
        if(StringUtils.isEmpty(urlString)||StringUtils.isEmpty(filename)||StringUtils.isEmpty(token)){
            return null;

        }else {
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
            return readInputStream(is);
        }
    }

    public static void main(String[] args) {

    }
}
