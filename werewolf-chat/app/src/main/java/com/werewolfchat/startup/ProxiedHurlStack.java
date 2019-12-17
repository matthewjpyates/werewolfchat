package com.werewolfchat.startup;


// stole this from https://stackoverflow.com/questions/23914407/volley-behind-a-proxy-server


import com.android.volley.toolbox.HurlStack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxiedHurlStack extends HurlStack {

    private int proxyPort;
    private String proxyURL;
    private int timeOut;

    public ProxiedHurlStack(int proxy)
    {
        this.proxyPort = proxy;
        this.proxyURL = "127.0.0.1";
        this.timeOut = 2000;
    }

    public ProxiedHurlStack(int proxy, int to)
    {
        this.proxyPort = proxy;
        this.proxyURL = "127.0.0.1";
        this.timeOut = to;
    }

    public ProxiedHurlStack(String url, int proxy)
    {
        this.proxyPort = proxy;
        this.proxyURL = url;
        this.timeOut = 2000;
    }



    public ProxiedHurlStack(String url, int proxy, int to)
    {
        this.proxyPort = proxy;
        this.proxyURL = url;
        this.timeOut = to;
    }


    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {

        // Start the connection by specifying a proxy server
        Proxy proxy = new Proxy( this.proxyPort ==9050 ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(this.proxyURL, this.proxyPort));//the proxy server(Can be your laptop ip or company proxy)
        HttpURLConnection returnThis = (HttpURLConnection) url
                .openConnection(proxy);

        returnThis.setReadTimeout(timeOut);

        return returnThis;
    }
}
