/**
 * 
 */
package net.ypb.contactselector;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preference activity.
 * 
 * @author Anders
 *
 */
public class Settings extends PreferenceActivity 
{

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
	}
}
