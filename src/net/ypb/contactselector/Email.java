/**
 * 
 */
package net.ypb.contactselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * A wrapper activity to send an email back to developer.
 * 
 * @author Anders
 *
 */
public class Email extends Activity
{
	public static final String EMAIL_ADDRESS = "contact.selector@gmail.com"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see android.app.AliasActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("plain/text");   //$NON-NLS-1$
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_ADDRESS});
		
		startActivity(Intent.createChooser(i, getText(R.string.send_email_title)));
		
		finish();
	}

}
