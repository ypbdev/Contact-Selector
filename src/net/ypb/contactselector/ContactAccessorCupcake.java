/**
 * 
 */
package net.ypb.contactselector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.android.Utilities;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;

/**
 * @author Anders
 *
 */
public class ContactAccessorCupcake extends ContactAccessor
{

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#fillEmailData(android.database.Cursor, java.util.List, android.app.Activity)
	 */
	@Override
	protected void fillEmailData(Cursor contact, List<Map<String, String>> data, Activity activity)
	{
		contact.moveToFirst();
		while (!contact.isAfterLast())
		{
			String name = contact.getString(contact.getColumnIndex(People.DISPLAY_NAME));
			String id = contact.getString(contact.getColumnIndex(People._ID));
			
			if (name!=null && !name.contains("@"))
			{
				Uri uri = ContactMethods.CONTENT_EMAIL_URI;
				String[] projection = new String[]
					{	ContactMethods.PERSON_ID, 
						ContactMethods.DATA,
						ContactMethods.TYPE
					};
				String selection = ContactMethods.PERSON_ID + " = ?";
				String[] selectionArgs = new String[]{id};
				String sortOrder = Contacts.People.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
				sortOrder = null;
				
				Cursor c = activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
				
				if (!c.isAfterLast())
				{
					c.moveToFirst();
					String email = c.getString(c.getColumnIndex(ContactMethods.DATA));
					int type = c.getInt(c.getColumnIndex(ContactMethods.TYPE));
					String emailType = "";
					
					switch (type)
					{
						case ContactMethods.TYPE_HOME:
							emailType = activity.getString(R.string.home);
							break;
						case ContactMethods.TYPE_WORK:
							emailType = activity.getString(R.string.work);
							break;
						case ContactMethods.TYPE_OTHER:
							emailType = activity.getString(R.string.other);
							break;
						case ContactMethods.TYPE_CUSTOM:
							emailType = activity.getString(R.string.other);
							break;
	
						default:
							break;
					}
					
					HashMap<String, String> listItemMap = new HashMap<String, String>();
					listItemMap.put(NAME, name);
					listItemMap.put(INFO, email+INFO_SEPARATOR+emailType);
					listItemMap.put(CONTACT_ID, id);
					data.add(listItemMap);
					
					c.close();
				}
			}

			contact.moveToNext();
		}
	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#fillPhoneData(android.database.Cursor, java.util.List, android.app.Activity)
	 */
	@Override
	protected void fillPhoneData(Cursor contact, List<Map<String, String>> data, Activity activity)
	{
		contact.moveToFirst();
		while (!contact.isAfterLast())
		{
			String name = contact.getString(contact.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
			String number = contact.getString(contact.getColumnIndex(Contacts.Phones.NUMBER));
			int type = contact.getInt(contact.getColumnIndex(Contacts.Phones.TYPE));
			String id = contact.getString(contact.getColumnIndex(Contacts.Phones.PERSON_ID));
			
			String numberType = "";
			switch (type)
			{
				case Contacts.Phones.TYPE_MOBILE:
					{
						numberType = activity.getString(R.string.mobile);
						break;
					}
				case Contacts.Phones.TYPE_OTHER:
					{
						numberType = activity.getString(R.string.other);
						break;
					}
				default:
					{
						break;
					}
			}

			HashMap<String, String> listItemMap = new HashMap<String, String>();
			listItemMap.put(NAME, name);
			listItemMap.put(INFO, number+INFO_SEPARATOR+numberType);
			listItemMap.put(CONTACT_ID, id);
			data.add(listItemMap);
			
			contact.moveToNext();
		}

	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#getContactLookupUri()
	 */
	@Override
	public Uri getContactLookupUri()
	{
		return People.CONTENT_URI;
	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#getContacts(android.app.Activity)
	 */
	@Override
	protected Cursor getContacts(Activity activity)
	{
		// Run query
		Uri uri = People.CONTENT_URI;
		String[] projection = new String[]
			{	Contacts.Phones.PERSON_ID, 
				Contacts.Phones.DISPLAY_NAME,
				Contacts.Phones.TYPE
			};
		projection = null;
		String selection = Contacts.Phones.TYPE + " = " + 
					"'"+String.valueOf(Contacts.Phones.TYPE_MOBILE)+"'" +
					" OR " +
					Contacts.Phones.TYPE + " = " + 
					"'"+String.valueOf(Contacts.Phones.TYPE_OTHER)+"'";
		selection = null;
		String[] selectionArgs = null;//new String[]{"'"+String.valueOf(Contacts.Phones.TYPE_MOBILE)+"'", "'"+String.valueOf(Contacts.Phones.TYPE_OTHER)+"'"};
		String sortOrder = Contacts.People.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		sortOrder = null;
		
		Cursor c = activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
		Utilities.getInstance().printCursor(c, false);
		return c;
	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#fillData(net.ypb.contactselector.ContentKind, android.app.Activity)
	 */
	@Override
	public List<Map<String, String>> fillData(ContentKind content, Activity activity)
	{
		List<Map<String, String>> results = new ArrayList<Map<String,String>>();
		if (ContentKind.PHONE==content)
		{
			Uri uri = Contacts.Phones.CONTENT_URI;
			String[] projection = new String[]
				{	Contacts.Phones.PERSON_ID, 
					Contacts.Phones.DISPLAY_NAME,
					Contacts.Phones.NUMBER,
					Contacts.Phones.TYPE
				};
			String selection = Contacts.Phones.TYPE + " = " + 
						"'"+String.valueOf(Contacts.Phones.TYPE_MOBILE)+"'" +
						" OR " +
						Contacts.Phones.TYPE + " = " + 
						"'"+String.valueOf(Contacts.Phones.TYPE_OTHER)+"'";
			String[] selectionArgs = null;//new String[]{"'"+String.valueOf(Contacts.Phones.TYPE_MOBILE)+"'", "'"+String.valueOf(Contacts.Phones.TYPE_OTHER)+"'"};
			String sortOrder = Contacts.People.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

			Cursor c = activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
			fillPhoneData(c, results, activity);
			c.close();
		}
		else if (ContentKind.EMAIL==content)
		{
			// Run query
			Uri uri = People.CONTENT_URI;
			String[] projection = new String[]
				{	People._ID, 
					People.DISPLAY_NAME
				};
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = Contacts.People.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
			
			Cursor c = activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
			fillEmailData(c, results, activity);
			c.close();
		}
		return results;
	}
}
