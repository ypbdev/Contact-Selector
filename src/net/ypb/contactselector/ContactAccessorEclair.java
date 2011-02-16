/**
 * 
 */
package net.ypb.contactselector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Encapsulates the Contacts API for level 2.0 (Eclair) and above.
 * 
 * @author Anders
 *
 */
public class ContactAccessorEclair extends ContactAccessor
{
	//private static final String TAG = "ContactAccessorEclair";
	
	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#fillEmailData(android.database.Cursor, java.util.List, android.app.Activity)
	 */
	@Override
	public void fillEmailData(Cursor contact, List<ListItem> data, Activity activity)
	{
		String id = contact.getString(contact.getColumnIndex(ContactsContract.Contacts._ID));
		String name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		
		// get the email addresses for the given contact
		Cursor email = activity.managedQuery( 
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				new String[]{ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.DATA3},
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",  //$NON-NLS-1$
				new String[]{id}, null); 
		
		email.moveToFirst();
		Set<String> addresses = new HashSet<String>(); // don't show duplicate email addresses
		while (!email.isAfterLast())
		{
			String address = email.getString(email.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			if (addresses.contains(address))
			{
				email.moveToNext();
				continue;
			}
			else
			{
				addresses.add(address);
			}
			
			// make sure it's data we're interested in
			String addressType = ""; //$NON-NLS-1$
			int type = email.getInt(email.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
			boolean isInterested = false;
			switch (type)
			{
				case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
					{
						addressType = activity.getString(R.string.mobile);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
					{
						addressType = activity.getString(R.string.work);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
					{
						addressType = activity.getString(R.string.other);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
					{
						addressType = activity.getString(R.string.home);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM:
					{
						addressType = email.getString(email.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA3));
						if (addressType==null)
						{
							addressType = activity.getString(R.string.custom);
						}
						isInterested = true;
						break;
					}
				default:
					{
						break;
					}
			}
		
			// fill the data structure
			if (isInterested)
			{
				data.add(new ListItem(name, address+INFO_SEPARATOR+addressType, id));
			}
			email.moveToNext();
		}
		email.close();
	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#fillPhoneData(android.database.Cursor, java.util.List, android.app.Activity)
	 */
	@Override
	public void fillPhoneData(Cursor contact, List<ListItem> data, Activity activity)
	{
		String id = contact.getString(contact.getColumnIndex(ContactsContract.Contacts._ID));
		String name = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		
		// get the phone numbers for the given contact
		Cursor phone = activity.managedQuery(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.DATA3}, 
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",  //$NON-NLS-1$
				new String[]{id}, 
				null);

		// make sure we're interested in the data
		boolean isShowAll = isShowAllNumbers(activity);
		phone.moveToFirst();
		while (!phone.isAfterLast())
		{
			String number = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			String numberType = ""; //$NON-NLS-1$
			int type = phone.getInt(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
			boolean isInterested = false;
			switch (type)
			{
				case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
					{
						numberType = activity.getString(R.string.mobile);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
					{
						numberType = activity.getString(R.string.work_mobile);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
					{
						numberType = activity.getString(R.string.other);
						isInterested = true;
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.assistant);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.callback);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.car);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.company);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.fax_home);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.fax_work);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.home);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.isdn);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.main);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.mms);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.other_fax);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.pager);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.radio);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.telex);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.tty_tdd);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.work);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
					{
						if (isShowAll)
						{
							numberType = activity.getString(R.string.work_pager);
							isInterested = true;
						}
						break;
					}
				case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
					{
						if (isShowAll)
						{
							numberType = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA3));
							if (numberType==null)
							{
								numberType = activity.getString(R.string.custom);
							}
							isInterested = true;
						}
						break;
					}
				default:
					{
						break;
					}
			}
		
			// fill the returning data structure
			if (isInterested)
			{
				data.add(new ListItem(name, number+INFO_SEPARATOR+numberType, id));
			}
			phone.moveToNext();
		}
		phone.close();
	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#getContacts(android.app.Activity)
	 */
	@Override
	public Cursor getContacts(Activity activity, String selection)
	{
		// Get all the "visible" contacts
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[]
			{	ContactsContract.Contacts._ID, 
				ContactsContract.Contacts.DISPLAY_NAME
			};
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"; //$NON-NLS-1$
		return activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
	}

	/* (non-Javadoc)
	 * @see net.ypb.contactselector.ContactAccessor#getContactLookupUri()
	 */
	@Override
	public Uri getContactLookupUri()
	{
		return ContactsContract.Contacts.CONTENT_URI;
	}

}
