/**
 * 
 */
package net.ypb.contactselector;

import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * An encapsulation of the Contacts API.
 * 
 * @author Anders
 *
 */
public abstract class ContactAccessor implements ContactConstants
{
	@SuppressWarnings("unused")
	private static final String TAG = "ContactAccessor"; //$NON-NLS-1$
	
	protected static ContactAccessor _self;
	
	/**
	 * @return the correct ContactAccessor for the OS level.
	 */
	public static ContactAccessor getInstance()
	{
		if (_self==null)
		{
            String className = null;
            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
            if (sdkVersion >= Build.VERSION_CODES.ECLAIR) 
            {
                className = "ContactAccessorEclair"; //$NON-NLS-1$
            }
            else
            {
            	// only support 2.0 and above
            	return null;
            }
            
            try 
            {
                Class<? extends ContactAccessor> clazz =
                        Class.forName(PACKAGE + "." + className) //$NON-NLS-1$
                                .asSubclass(ContactAccessor.class);
                _self = clazz.newInstance();
            } 
            catch (Exception e) 
            {
                throw new IllegalStateException(e);
            }		
		}
		return _self;
	}
	
	/**
	 * Obtains the contact list for the currently selected account.
	 * 
	 * @param activity - used to manage the cursor.
	 * @param selection - selection string used for contact queries. 
	 * @return A cursor for for accessing the contact list.
	 */
	public abstract Cursor getContacts(Activity activity, String selection);
	
	/**
	 * Obtain the info for displayable phone numbers.
	 * 
	 * @param contact - one row in the contacts table
	 * @param data - construct to add phone data to
	 * @param activity - used to manage cursors
	 */
	public abstract void fillPhoneData(Cursor contact, List<ListItem> data, Activity activity);
	
	/**
	 * Obtain the info for displayable email addresses.
	 *
	 * @param contact - one row in the contacts table
	 * @param data - construct to add phone data to
	 * @param activity - used to manage cursors
	 */
	public abstract void fillEmailData(Cursor contact, List<ListItem> data, Activity activity);
	
	public abstract Uri getContactLookupUri();
	
	public boolean isShowAllNumbers(Activity activity)
	{
		String dflt = PreferenceManager.getDefaultSharedPreferences(activity).getString("phone_types", activity.getString(R.string.phone_type_default)); //$NON-NLS-1$
		String[] values = activity.getResources().getStringArray(R.array.entryvalues_phone_types);
		if (values[0].equals(dflt))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
