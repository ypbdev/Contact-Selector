package net.ypb.contactselector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactSelector extends Activity implements ContactConstants
{
	/**
	 * Class used to maintain state between activity instances and/or between threads.
	 * 
	 * @author anders
	 *
	 */
	private class ContactConfiguration
	{
		public ContentKind kind;
		public List<ListItem> data;
		public Activity activity;
		
		public ContactConfiguration(List<ListItem> data, ContentKind kind, Activity activity)
		{
			this.data = data;
			this.kind = kind;
			this.activity = activity;
		}
	}
	
	/**
	 * Class used to remove the window displaying the letter while scrolling.
	 * 
	 * @author anders
	 *
	 */
	private final class RemoveWindow implements Runnable 
    {
        public void run() 
        {
            removeWindow();
        }
    }

	/**
	 * Scroller to display the first letter of the name being displayed at the
	 * top of the screen.
	 * 
	 * @author anders
	 *
	 */
	private class ListScroller implements ListView.OnScrollListener
	{
		protected char _prevLetter = Character.MIN_VALUE;;
		protected RemoveWindow _removeWindow = new RemoveWindow();
		
		/**
		 * 
		 */
		public ListScroller()
		{
			super();
		}

		/* (non-Javadoc)
		 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
		 */
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
		{
			ListItem data = (ListItem)_contactList.getAdapter().getItem(firstVisibleItem);
			if (isReadyScrollPosition() && data!=null)
			{
				if (data instanceof LoadingListItem)
				{
					return;
				}
				char firstLetter = data.name.charAt(0);
				if (!isShowScrollPosition() && firstLetter!=_prevLetter)
				{
					setShowScrollPosition(true);
					getPositionText().setVisibility(View.VISIBLE);
				}
				getPositionText().setText(((Character)firstLetter).toString());
				getHandler().removeCallbacks(_removeWindow);
				getHandler().postDelayed(_removeWindow, 2000);
				_prevLetter = firstLetter;
			}
		}

		/* (non-Javadoc)
		 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
		 */
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
		}
	}
	
	/**
	 * Adapter to handle displaying the list of contacts.
	 * 
	 * @author anders
	 *
	 */
	private class ContactAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			if (_listData==null)
			{
				return 0;
			}
			int count = _listData.size();
			return count;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position)
		{
			int count = getCount();
			if (getCount()>0 &&
					position>=0 &&
					position<count)
			{
				return _listData.get(position);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position)
		{
			ListItem row = (ListItem)getItem(position);
			if (row!=null)
			{
				return row.contact_id.hashCode() + row.info.hashCode();
			}
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (getCount()==0)
			{
				return convertView;
			}
			View view = null;
			Object item = getItem(position);
			if (item instanceof LoadingListItem)
			{
				return _loadingView;
			}
			else if (_loadingView.equals(convertView))
			{
				// this isn't the loading item but the loading view is being converted,
				// nullify to ensure a list item gets created.
				convertView = null;
			}
			
			if (convertView==null)
			{
				view = getLayoutInflater().inflate(R.layout.list_item, null);
			}
			else
			{
				view = convertView;
			}
			
			TextView name = (TextView)view.findViewById(R.id.listItemName);
			TextView info = (TextView)view.findViewById(R.id.listItemInfo);
			CheckBox check = (CheckBox)view.findViewById(R.id.listItemCheck);
			
			ListItem data = (ListItem)getItem(position);
			
			name.setText(data.name);
			info.setText(data.info);

			check.setOnCheckedChangeListener(null);
			check.setChecked(_selected.contains(String.valueOf(position)));
			check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					ViewParent linearLayout = buttonView.getParent();
					ListView listView = (ListView)linearLayout.getParent();
					int pos = listView.getPositionForView(buttonView);
					if (listView.getCount()>0)
					{
						// if the list has content then the header is in the children list
						pos--;
					}
					setSelected(pos, isChecked);
				}
			});
			
			return view;
		}
	}
	
	/**
	 * Task to handle querying and parsing the contacts.
	 * 
	 * @author anders
	 *
	 */
	private class ReadContactsTask extends AsyncTask<ContactConfiguration, List<ListItem>, ContactConfiguration>
	{
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected ContactConfiguration doInBackground(ContactConfiguration... payload)
		{
			String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'"; //$NON-NLS-1$
			if (payload[0].kind==ContentKind.PHONE)
			{
				selection += " AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";  //$NON-NLS-1$//$NON-NLS-2$
			}
			Cursor contacts = ContactAccessor.getInstance().getContacts(payload[0].activity, selection);
			if (contacts.moveToFirst())
			{
				ContentKind content = payload[0].kind;
				List<ListItem> data = new ArrayList<ListItem>();
				Activity activity = payload[0].activity;
				
				int counter = 0;
				while (!contacts.isAfterLast())
				{
					if (content==ContentKind.PHONE)
					{
						ContactAccessor.getInstance().fillPhoneData(contacts, data, activity);
					}
					else if (content==ContentKind.EMAIL)
					{
						ContactAccessor.getInstance().fillEmailData(contacts, data, activity);
					}
					counter++;
					if ((counter % 10)==0)
					{
						// copy the data to send to the UI thread to display
						List<ListItem> uiData = new ArrayList<ListItem>(data.size());
						for (ListItem item : data)
						{
							uiData.add(item);
						}
						publishProgress(uiData);
						data.clear();
					}
					contacts.moveToNext();
				}
				
				if (!data.isEmpty())
				{
					publishProgress(data);
				}
			}
			contacts.close();
			return payload[0];
		}

		protected void onProgressUpdate(java.util.List<ListItem>[] values) 
		{
			_listData.addAll(values[0]);
			_listData.remove(_loadingItem); // remove
			_listData.add(_loadingItem); // add back to end
			ContactAdapter adapter = (ContactAdapter)((HeaderViewListAdapter)_contactList.getAdapter()).getWrappedAdapter();
			adapter.notifyDataSetChanged(); // tell the adapter we've changed the underlying data
			refreshList(_listData);
			_contactList.invalidateViews();
		};
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(ContactConfiguration payload)
		{
			_listData.remove(_loadingItem); // done loading, remove the loading view
			ContactAdapter adapter = (ContactAdapter)((HeaderViewListAdapter)_contactList.getAdapter()).getWrappedAdapter();
			adapter.notifyDataSetChanged(); // tell the adapter we've changed the underlying data
			refreshList(_listData);
			_contactList.invalidateViews();
			setProgressBarIndeterminateVisibility(false);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute()
		{
			setProgressBarIndeterminateVisibility(true);
			_listData.add(_loadingItem); // starting loading, add the "Loading..." item
		}
	}
	
	public static final String TAG = "ContactSelector"; //$NON-NLS-1$
	
	protected ContentKind _defaultContent = ContentKind.PHONE;
	protected ContentKind _contentKind;
	protected Set<String> _selected; 
	protected ListView _contactList;
	protected boolean _useTitlebarProgress;
	protected boolean _showScrollPosition;
	protected WindowManager _windowManager;
	protected ClipboardManager _clipboardManager;
	protected boolean _readyScrollPosition;
	protected List<ListItem> _listData;
	protected TextView _positionText;
	protected TextView _headerView;
	protected View _loadingView;
	protected Handler _handler;
	protected ReadContactsTask _contactsTask;
	protected LoadingListItem _loadingItem = new LoadingListItem();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		_windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		_clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		
		setContentView(R.layout.main);

		_contactList = (ListView)findViewById(R.id.contactList);
		_contactList.setEmptyView(findViewById(R.id.empty_list_view_text));
		
		_headerView = new TextView(this);
		_headerView.setGravity(Gravity.CENTER_HORIZONTAL);
		_contactList.addHeaderView(_headerView);
		
		_loadingView = getLayoutInflater().inflate(R.layout.empty_list_item, null);
		refreshLoadingView();
		
		Object storedData = getLastNonConfigurationInstance();
		if (storedData!=null)
		{
			ContactConfiguration config = (ContactConfiguration)storedData;
			_listData = config.data;
			_contentKind = config.kind;
		}
		
		if (_listData==null)
		{
			_listData = new ArrayList<ListItem>();
			_contactsTask = new ReadContactsTask();
			_contactsTask.execute(new ContactConfiguration(_listData, getContentKind(), this));
		}
		refreshList(_listData);
		_contactList.setAdapter(new ContactAdapter());
		_contactList.setLayoutAnimation(getListAnimation());
		_contactList.setOnScrollListener(new ListScroller());

		_selected = new HashSet<String>();

		_handler = new Handler();
        _handler.post(new Runnable() 
        {
            public void run() 
            {
                setReadyScrollPosition(true);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                _positionText = null;
                _windowManager.addView(getPositionText(), lp);
            }
        });
        _contactList.setOnItemLongClickListener(new OnItemLongClickListener()
			{
        		@Override
        		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        		{
        			String cid = _listData.get(position-1).contact_id; // position-1 for the entries view
        			Uri contactUri = Uri.withAppendedPath(ContactAccessor.getInstance().getContactLookupUri(), cid);
        			Intent i = new Intent(Intent.ACTION_VIEW, contactUri);
        			if (isIntentAvailable(i))
        			{
        				startActivity(i);
        			}
        			return true;
        		}
			});
        
		Button copy = (Button)findViewById(R.id.copyClipboardButton);
		copy.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Log.d(TAG, "copyClipboardButton pressed"); //$NON-NLS-1$
					
					StringBuffer sBuffer = new StringBuffer();
					List<String> results = performCopyToClipboard(null, sBuffer);
					int copied = results.size();
					String title = null;
					Intent i = null;
					String text = ""; //$NON-NLS-1$
					boolean launchApp = false;
					if (getContentKind()==ContentKind.EMAIL)
					{
						text = (copied==1?getString(R.string.email_copied):getString(R.string.emails_copied));
						i = new Intent(Intent.ACTION_SEND);
						i.setType("plain/text");   //$NON-NLS-1$
						i.putExtra(Intent.EXTRA_EMAIL, results.toArray(new String[results.size()]));
						title = (String)getText(R.string.send_email_title);
						
						launchApp = PreferenceManager.getDefaultSharedPreferences(ContactSelector.this).getBoolean("launch_email_app", true); //$NON-NLS-1$
					}
					else if (getContentKind()==ContentKind.PHONE)
					{
						text = (copied==1?getString(R.string.number_copied):getString(R.string.numbers_copied));
						Uri smsUri = Uri.parse("sms:"+sBuffer.toString());   //$NON-NLS-1$
						i = new Intent(Intent.ACTION_VIEW, smsUri); 
						i.putExtra("address", sBuffer.toString()); //$NON-NLS-1$
						i.setType("vnd.android-dir/mms-sms");   //$NON-NLS-1$
						title = (String)getText(R.string.send_sms_title);
						
						launchApp = PreferenceManager.getDefaultSharedPreferences(ContactSelector.this).getBoolean("launch_sms_app", true); //$NON-NLS-1$
					}
					else
					{
						text = (copied==1?getString(R.string.item_copied):getString(R.string.items_copied));
					}
					text = String.format(text, copied);

					Toast toast = Toast.makeText(ContactSelector.this, text, Toast.LENGTH_SHORT);
					toast.show();
					
					if (copied>0 && launchApp)
					{
						startActivity(Intent.createChooser(i, title));
					}
					else
					{
						setResult(RESULT_OK);
						finish();
					}
				}
			});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
	
	protected void refreshLoadingView()
	{
		TextView loadingText = (TextView)_loadingView.findViewById(R.id.empty_list_progress_text);
		loadingText.setText(getProgressText());
	}
	
	protected synchronized void refreshList(List<ListItem> listData)
	{
		String headerText = ""; //$NON-NLS-1$
		if (listData.size()!=1)
		{
			headerText = getString(R.string.display_entries, listData.size());
		}
		else
		{
			headerText = getString(R.string.display_entry, listData.size());
		}
		_headerView.setText(headerText);
		
		TextView empty = (TextView)findViewById(R.id.empty_list_view_text);
		if (listData.size()==0)
		{
			if (ContentKind.EMAIL==getContentKind())
			{
				empty.setText(getText(R.string.emptyList_email));
			}
			else if (ContentKind.PHONE==getContentKind())
			{
				empty.setText(getText(R.string.emptyList_phone));
			}
			else
			{
				empty.setText(getText(R.string.emptyList));
			}
			empty.setVisibility(View.VISIBLE);
		}
		else
		{
			empty.setVisibility(View.GONE);
		}
	}
	
	protected List<String> performCopyToClipboard(Bundle bundle, StringBuffer buffer)
	{
		int result = 0;
		List<String> entries = new ArrayList<String>();
		if (_selected==null)
		{
			return entries;
		}
		StringBuffer clipboard = new StringBuffer();
		for (Iterator<String> iter = _selected.iterator(); iter.hasNext();)
		{
			if (result>0)
			{
				clipboard.append(", "); //$NON-NLS-1$
				buffer.append(", "); //$NON-NLS-1$
			}
			
			String sPos = iter.next();
			int pos = Integer.valueOf(sPos).intValue();
			ListItem data = _listData.get(pos);
			String name = data.name;
			String info = data.info;
			info = info.substring(0, info.indexOf(INFO_SEPARATOR));
			String entry = name + " " + INFO_OPEN + info + INFO_CLOSE; //$NON-NLS-1$
			clipboard.append(entry);
			buffer.append(info);
			entries.add(entry);
			
			result++;
		}
		
		if (result>0)
		{
			if (bundle!=null)
			{
				bundle.putString("result", clipboard.toString()); //$NON-NLS-1$
			}
			else
			{
				_clipboardManager.setText(clipboard.toString());
			}
		}
		return entries;
	}
	
	/**
	 * @return the contentKind
	 */
	protected ContentKind getContentKind()
	{
		if (_contentKind==null)
		{
			String dflt = PreferenceManager.getDefaultSharedPreferences(this).getString("default_display", getString(R.string.contact_type_default)); //$NON-NLS-1$
			String[] values = getResources().getStringArray(R.array.entryvalues_default_display);
			if (values[0].equals(dflt))
			{
				_contentKind = ContentKind.PHONE;
			}
			else if (values[1].equals(dflt))
			{
				_contentKind = ContentKind.EMAIL;
			}
			else
			{
				_contentKind = _defaultContent;
			}
		}
		return _contentKind;
	}

	/**
	 * @param contentKind the contentKind to set
	 */
	protected void setContentKind(ContentKind contentKind)
	{
		_contentKind = contentKind;
	}

	protected LayoutAnimationController getListAnimation()
	{
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(150);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(300);
        set.addAnimation(animation);

        return new LayoutAnimationController(set, 0.5f);
	}

	/**
	 * @return the showScrollPosition
	 */
	public boolean isShowScrollPosition()
	{
		return _showScrollPosition;
	}

	/**
	 * @param showScrollPosition the showScrollPosition to set
	 */
	public void setShowScrollPosition(boolean showScrollPosition)
	{
		_showScrollPosition = showScrollPosition;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		setReadyScrollPosition(false);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		removeWindow();
		setReadyScrollPosition(false);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		setReadyScrollPosition(true);
	}

	/**
	 * @return the readyScrollPosition
	 */
	public boolean isReadyScrollPosition()
	{
		return _readyScrollPosition;
	}

	/**
	 * @param readyScrollPosition the readyScrollPosition to set
	 */
	public void setReadyScrollPosition(boolean readyScrollPosition)
	{
		_readyScrollPosition = readyScrollPosition;
	}

	/**
	 * @return the positionText
	 */
	public TextView getPositionText()
	{
		if (_positionText==null)
		{
	        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        
	        _positionText = (TextView) inflate.inflate(R.layout.list_position, null);
	        _positionText.setVisibility(View.INVISIBLE);
		}
		return _positionText;
	}
	
    private void removeWindow() 
    {
        if (isShowScrollPosition()) 
        {
            setShowScrollPosition(false);
            getPositionText().setVisibility(View.INVISIBLE);
        }
    }

	/**
	 * @return the handler
	 */
	public Handler getHandler()
	{
		return _handler;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem sn = menu.findItem(R.id.select_none);
		MenuItem sa = menu.findItem(R.id.select_all);
		
		if (_selected==null)
		{
			// can't (de)select an empty list
			sn.setEnabled(false);
			sa.setEnabled(false);
		}
		else
		{
			// only deselect if there's something selected
			sn.setEnabled(!_selected.isEmpty());
			sa.setEnabled(true);
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.select_all:
				performSelectAll(true);
				return true;
			case R.id.select_none:
				performSelectAll(false);
				return true;
			case R.id.contact_type_email:
			{
				setContentKind(ContentKind.EMAIL);
				refreshLoadingView();
				_listData = new ArrayList<ListItem>();
				_contactsTask = new ReadContactsTask();
				_contactsTask.execute(new ContactConfiguration(_listData, getContentKind(), this));
				return true;
			}
			case R.id.contact_type_number:
			{
				setContentKind(ContentKind.PHONE);
				refreshLoadingView();
				_listData = new ArrayList<ListItem>();
				_contactsTask = new ReadContactsTask();
				_contactsTask.execute(new ContactConfiguration(_listData, getContentKind(), this));
		        return true;
			}
			case R.id.about:
			{
				Intent i = new Intent(this, Settings.class);
				startActivity(i);
			}
			default:
				break;
		}
		
		return false;
	}

	protected void performSelectAll(boolean selected)
	{
		// set the visible check boxes
		for (int i=0; i<_contactList.getCount(); i++)
		{
			View child = _contactList.getChildAt(i);
			if (child!=null)
			{
				CheckBox check = (CheckBox)child.findViewById(R.id.listItemCheck);
				if (check!=null)
				{
					check.setChecked(selected);
				}
			}
		}
		
		// set the state array
		for (int i=0; i<_listData.size(); i++)
		{
			setSelected(i, selected);
		}
	}
	
	private void setSelected(int position, boolean selected)
	{
		String sPos = String.valueOf(position);
		if (selected)
		{
			_selected.add(sPos);
		}
		else
		{
			_selected.remove(sPos);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		if (_contactsTask!=null)
		{
			Status status = _contactsTask.getStatus();
			_contactsTask.cancel(true);
			if (Status.FINISHED!=status)
			{
				_listData = null;
			}
		}
		ContactConfiguration config = new ContactConfiguration(_listData, getContentKind(), null);
		
		return config;
	}
	
	protected String getProgressText()
	{
		String text = getString(R.string.reading_contacts);
		if (ContentKind.PHONE==getContentKind())
		{
			if (ContactAccessor.getInstance().isShowAllNumbers(this))
			{
				text = getString(R.string.reading_contacts_phone);
			}
			else
			{
				text = getString(R.string.reading_contacts_sms);
			}
		}
		else if (ContentKind.EMAIL==getContentKind())
		{
			text = getString(R.string.reading_contacts_email);
		}
		return text;
	}
	
	protected boolean isIntentAvailable(Intent intent) 
	{
	    final PackageManager packageManager = getPackageManager();
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
}