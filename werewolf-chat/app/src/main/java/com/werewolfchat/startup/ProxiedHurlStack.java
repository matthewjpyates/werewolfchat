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
    private boolean isSOCKS;

    public ProxiedHurlStack(int proxy)
    {
        this.proxyPort = proxy;
        this.proxyURL = "127.0.0.1";
        this.timeOut = 2000;

        if(proxyPort == 4444 )
            isSOCKS = false;
        else
            isSOCKS = true;
        Utility.dumb_debugging("using  contructor -1");

    }

    public ProxiedHurlStack(int proxy, int to)
    {
        this.proxyPort = proxy;
        this.proxyURL = "127.0.0.1";
        this.timeOut = to;

        if(proxyPort == 4444 )
            isSOCKS = false;
        else
            isSOCKS = true;
        Utility.dumb_debugging("using  contructor 0");
    }



    public ProxiedHurlStack(String url, int proxy)
    {
        this.proxyPort = proxy;
        this.proxyURL = url;
        this.timeOut = 2000;
        if(proxyPort == 4444 )
            isSOCKS = false;
        else
            isSOCKS = true;
        Utility.dumb_debugging("using  contructor 2");

    }


    public ProxiedHurlStack(String url, int proxy, int to, String proxyType)
    {
        Utility.dumb_debugging("the passed in proxy type is "+proxyType);
        if(proxyType.toUpperCase().equals("SOCKS"))
        {
            Utility.dumb_debugging(" I am a socks proxy, so this is probably a tor connection...");
            isSOCKS = true;
        }
        else
        {
            Utility.dumb_debugging(" I am a http proxy, so this is probably a i2p connection...");

            isSOCKS = false;
        }
        this.proxyPort = proxy;
        Utility.dumb_debugging("Proxy port:  " + Integer.toString(this.proxyPort));

        this.proxyURL = url;
        Utility.dumb_debugging("Proxy URL:  " + this.proxyURL);
        this.timeOut = to;
        Utility.dumb_debugging("Proxy timeout:  " + Integer.toString(this.timeOut));

        Utility.dumb_debugging("using  contructor 3");


    }


    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {

        // Start the connection by specifying a proxy server
        Proxy proxy = new Proxy( isSOCKS ? Proxy.Type.SOCKS : Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(this.proxyURL, this.proxyPort));//the proxy server(Can be your laptop ip or company proxy)
        HttpURLConnection returnThis = (HttpURLConnection) url
                .openConnection(proxy);

        returnThis.setReadTimeout(timeOut);

        return returnThis;
    }
}
