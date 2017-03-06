package thijsb.nrcmeestgelezen;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView w, String url) { //hide header and more from NRC
        if (url.startsWith("https://www.nrc.nl/nieuws") || url.startsWith("https://nrc.nl/nieuws")) {
            w.loadUrl(toCSS("header, .share, .feedback-button, footer, .banner, .tabbed-articles__section--type-popular-trending { display:none; }"));
        }
    }
    private String toCSS(String style) {
        return "javascript:(function(){ var node = document.createElement('style'); node.innerHTML = '" + style + "'; document.body.appendChild(node); })();";
    }
    public boolean shouldOverrideUrlLoading(WebView view, String url) { //load urls in default browser
        Log.d("url", url);
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))
                && !url.contains("nrc.nl")) {
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } else {
            return false;
        }
    }
}