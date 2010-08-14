/**
 * 
 */
package net.ypb.contactselector;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * @author Anders
 *
 */
public class HowTo extends Activity
{
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.webview);

	    WebView webview = (WebView) findViewById(R.id.webview);
	    
	    String html = ContactConstants.HTML_BODY_PRE +
	    				getString(R.string.howToBody) +
	    				ContactConstants.HTML_BODY_POST;
	    webview.loadData(html, "text/html", "UTF-8");
	}
}
