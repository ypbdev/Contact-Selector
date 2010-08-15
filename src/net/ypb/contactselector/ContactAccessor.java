/**
 * 
 */
package net.ypb.contactselector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/**
 * An encapsulation of the Contacts API.
 * 
 * @author Anders
 *
 */
public abstract class ContactAccessor implements ContactConstants
{
	private static final String TAG = "ContactAccessor";
	
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
                className = "ContactAccessorEclair";
            }
            else if (sdkVersion >= Build.VERSION_CODES.CUPCAKE)
            {
            	className = "ContactAccessorCupcake";
            }
            
            try 
            {
                Class<? extends ContactAccessor> clazz =
                        Class.forName(PACKAGE + "." + className)
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
	 * @param activity - used to manage the cursor
	 * @return A cursor for for accessing the contact list.
	 */
	protected abstract Cursor getContacts(Activity activity);
	
	/**
	 * Obtain the info for displayable phone numbers.
	 * 
	 * @param contact - one row in the contacts table
	 * @param data - construct to add phone data to
	 * @param activity - used to manage cursors
	 */
	protected abstract void fillPhoneData(Cursor contact, List<Map<String, String>> data, Activity activity);
	
	/**
	 * Obtain the info for displayable email addresses.
	 *
	 * @param contact - one row in the contacts table
	 * @param data - construct to add phone data to
	 * @param activity - used to manage cursors
	 */
	protected abstract void fillEmailData(Cursor contact, List<Map<String, String>> data, Activity activity);
	
	/**
	 * Return the data to display to the user.
	 * 
	 * @param content - the type of information to retrieve
	 * @param activity - used to manage the cursor
	 * @return list of email addresses or phone numbers, 
	 * 				Map keys:
	 * 				{@link NAME}, {@link INFO}, {@link CONTACT_ID}
	 */
	public List<Map<String, String>> fillData(ContentKind content, Activity activity)
	{
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		
		long start = System.currentTimeMillis();
		Cursor contacts = getContacts(activity);
		long end = System.currentTimeMillis();
		Log.v(TAG, "getContacts() took "+(end-start)+" ms.");

		start = System.currentTimeMillis();
		contacts.moveToFirst();
		while (!contacts.isAfterLast())
		{
			if (content==ContentKind.PHONE)
			{
				fillPhoneData(contacts, data, activity);
			}
			else if (content==ContentKind.EMAIL)
			{
				fillEmailData(contacts, data, activity);
			}
			
			contacts.moveToNext();
		}
		end = System.currentTimeMillis();
		Log.v(TAG, "Iterating through contacts took "+(end-start)+" ms.");
		
		contacts.close();
		return data;
	}
	
	public abstract Uri getContactLookupUri();
	
	public boolean isShowAllNumbers(Activity activity)
	{
		String dflt = activity.getSharedPreferences("net.ypb.contactselector_preferences", Activity.MODE_PRIVATE).getString("phone_types", activity.getString(R.string.phone_type_default));
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
