/**
 * 
 */
package net.ypb.contactselector;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Wrapper activity to launch a web view to display the "version history".
 * 
 * @author Anders
 *
 */
public class Version extends Activity
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
	    				getString(R.string.versionBody14) +
	    				getString(R.string.versionBody13) +
	    				getString(R.string.versionBody12) +
	    				getString(R.string.versionBody11) +
	    				getString(R.string.versionBody10) +
	    				ContactConstants.HTML_BODY_POST;
	    webview.loadData(html, "text/html", "UTF-8");
	}
}
