package io.udfs.api.util;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public  class ContentLengthHeaderRemover implements HttpRequestInterceptor {


    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        httpRequest.removeHeaders(HTTP.CONTENT_LEN);// fighting org.apache.http.protocol.RequestContent's ProtocolException("Content-Length header already present");
    }
}
