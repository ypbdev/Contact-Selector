package net.ypb.contactselector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask.Status;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class ContactSelector extends Activity implements ContactConstants
{
	private class ContactConfiguration
	{
		public ContentKind kind;
		public List<Map<String, String>> data;
		public Activity activity;
		
		public ContactConfiguration(List<Map<String, String>> data, ContentKind kind, Activity activity)
		{
			this.data = data;
			this.kind = kind;
			this.activity = activity;
		}
	}
	
	private final class RemoveWindow implements Runnable 
    {
        public void run() 
        {
            removeWindow();
        }
    }

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
			List<Map<String, String>> data = getListData();
			if (isReadyScrollPosition() && data!=null && data.size()>0)
			{
				char firstLetter = data.get(firstVisibleItem).get(NAME).charAt(0);
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
	
	private class MySimpleAdapter extends SimpleAdapter
	{
		protected boolean _updating;

		/**
		 * @param context
		 * @param data
		 * @param resource
		 * @param from
		 * @param to
		 */
		public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to)
		{
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = super.getView(position, convertView, parent);
			CheckBox check = (CheckBox)view.findViewById(R.id.listItemCheck);
			String pos = String.valueOf(position);
			_updating = true;
			check.setChecked(_selected.contains(pos));
			_updating = false;
			if (check!=null)
			{
				check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
					{
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
						{
							// ensure this is a user generated event
							if (_updating)
							{
								return;
							}
							ViewParent linearLayout = buttonView.getParent();
							ListView listView = (ListView)linearLayout.getParent();
							int pos = listView.getPositionForView(buttonView);
							if (listView.getCount()>0)
							{
								// if the list has content then the header is in the children list
								pos--;
							}
							setSelected(pos, isChecked);
							Log.v(TAG, "onCheckedChangeListener called "+buttonView+"\t"+pos);
						}
					});
			}
			return view;
		}
	}

	private class ReadContactsTask extends AsyncTask<ContactConfiguration, Integer, ContactConfiguration>
	{
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected ContactConfiguration doInBackground(ContactConfiguration... payload)
		{
			payload[0].data.addAll(ContactAccessor.getInstance().fillData(payload[0].kind, payload[0].activity));
			return payload[0];
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(ContactConfiguration payload)
		{
			setListAdapter(payload.data);
			setProgressBarIndeterminateVisibility(false);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute()
		{
			setProgressBarIndeterminateVisibility(true);
		}
		
	}
	
	public static final String TAG = "ContactSelector";
	
	protected ContentKind _defaultContent = ContentKind.PHONE;
	protected ContentKind _contentKind;
	protected Set<String> _selected; 
	protected ListView _contactList;
	protected boolean _useTitlebarProgress;
	protected boolean _showScrollPosition;
	protected WindowManager _windowManager;
	protected ClipboardManager _clipboardManager;
	protected boolean _readyScrollPosition;
	protected List<Map<String, String>> _listData;
	protected TextView _positionText;
	protected TextView _headerView;
	protected Handler _handler = new Handler();
	protected ReadContactsTask _contactsTask;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.v(TAG, "Activity State: onCreate()");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		_windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		_clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		
		setContentView(R.layout.main);

		_contactList = (ListView)findViewById(R.id.contactList);
		TextView progressText = (TextView)findViewById(R.id.empty_list_progress_text);
		progressText.setText(getProgressText());
		_contactList.setEmptyView(findViewById(R.id.empty_list_view));
		
		_headerView = new TextView(this);
		_headerView.setGravity(Gravity.CENTER_HORIZONTAL);
		_contactList.addHeaderView(_headerView);
		
		Object storedData = getLastNonConfigurationInstance();
		if (storedData!=null)
		{
			ContactConfiguration config = (ContactConfiguration)storedData;
			_listData = config.data;
			_contentKind = config.kind;
		}
		
		if (_listData==null)
		{
			_listData = new ArrayList<Map<String, String>>();
			_contactsTask = new ReadContactsTask();
			_contactsTask.execute(new ContactConfiguration(_listData, getContentKind(), this));
		}
		else
		{
			setListAdapter(_listData);
		}
		
		_contactList.setLayoutAnimation(getListAnimation());
		_contactList.setOnScrollListener(new ListScroller());
		
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
        			String cid = _listData.get(position-1).get(CONTACT_ID); // position-1 for the entries view
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
					Log.d(TAG, "copyClipboardButton pressed");
					
					StringBuffer sBuffer = new StringBuffer();
					List<String> results = performCopyToClipboard(null, sBuffer);
					int copied = results.size();
					String title = null;
					Intent i = null;
					String text = "";
					boolean launchApp = false;
					if (getContentKind()==ContentKind.EMAIL)
					{
						text = (copied==1?getString(R.string.email_copied):getString(R.string.emails_copied));
						i = new Intent(Intent.ACTION_SEND);
						i.setType("plain/text");  
						i.putExtra(Intent.EXTRA_EMAIL, results.toArray(new String[results.size()]));
						title = (String)getText(R.string.send_email_title);
						
						launchApp = getSharedPreferences("net.ypb.contactselector_preferences", MODE_PRIVATE).getBoolean("launch_email_app", true);
					}
					else if (getContentKind()==ContentKind.PHONE)
					{
						text = (copied==1?getString(R.string.number_copied):getString(R.string.numbers_copied));
						Uri smsUri = Uri.parse("sms:"+sBuffer.toString());  
						i = new Intent(Intent.ACTION_VIEW, smsUri); 
						i.putExtra("address", sBuffer.toString());
						i.setType("vnd.android-dir/mms-sms");  
						title = (String)getText(R.string.send_sms_title);
						
						launchApp = getSharedPreferences("net.ypb.contactselector_preferences", MODE_PRIVATE).getBoolean("launch_sms_app", true);
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

	protected synchronized void setListAdapter(List<Map<String, String>> listData)
	{
		_selected = new HashSet<String>(listData.size());

		String headerText = "";
		if (listData.size()!=1)
		{
			headerText = getString(R.string.display_entries, listData.size());
		}
		else
		{
			headerText = getString(R.string.display_entry, listData.size());
		}
		_headerView.setText(headerText);
		
		MySimpleAdapter adapter = new MySimpleAdapter(
				this,
				listData,
				R.layout.list_item,
				new String[]{ NAME, INFO },
				new int[] { R.id.listItemName, R.id.listItemInfo });
		
		if (listData.size()==0)
		{
			TextView empty = (TextView)findViewById(R.id.empty_list_view_text);
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
			_contactList.getEmptyView().setVisibility(View.GONE);
			_contactList.setEmptyView(empty);
		}
		_contactList.setAdapter(adapter);
		
	}
	
	protected List<String> performCopyToClipboard(Bundle bundle, StringBuffer buffer)
	{
		int result = 0;
		List<String> entries = new ArrayList<String>();
		StringBuffer clipboard = new StringBuffer();
		for (Iterator<String> iter = _selected.iterator(); iter.hasNext();)
		{
			if (result>0)
			{
				clipboard.append(", ");
				buffer.append(", ");
			}
			
			String sPos = iter.next();
			int pos = Integer.valueOf(sPos).intValue();
			Map<String, String> dataMap = _listData.get(pos);
			String name = dataMap.get(NAME);
			String info = dataMap.get(INFO);
			info = info.substring(0, info.indexOf(INFO_SEPARATOR));
			String entry = name + " " + INFO_OPEN + info + INFO_CLOSE;
			clipboard.append(entry);
			buffer.append(info);
			entries.add(entry);
			
			result++;
		}
		
		if (result>0)
		{
			if (bundle!=null)
			{
				bundle.putString("result", clipboard.toString());
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
			String dflt = getSharedPreferences("net.ypb.contactselector_preferences", MODE_PRIVATE).getString("default_display", getString(R.string.contact_type_default));
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
		Log.v(TAG, "Getting content kind: "+_contentKind.name());
		return _contentKind;
	}

	/**
	 * @param contentKind the contentKind to set
	 */
	protected void setContentKind(ContentKind contentKind)
	{
		Log.v(TAG, "Setting content kind to "+contentKind.name());
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
	 * @return the listData
	 */
	public List<Map<String, String>> getListData()
	{
		return _listData;
	}

	/**
	 * @param listData the listData to set
	 */
	public void setListData(List<Map<String, String>> listData)
	{
		_listData = listData;
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
		        _listData = new ArrayList<Map<String, String>>();
				_contactsTask = new ReadContactsTask();
				_contactsTask.execute(new ContactConfiguration(getListData(), getContentKind(), this));
				return true;
			}
			case R.id.contact_type_number:
			{
				setContentKind(ContentKind.PHONE);
		        _listData = new ArrayList<Map<String, String>>();
				_contactsTask = new ReadContactsTask();
				_contactsTask.execute(new ContactConfiguration(getListData(), getContentKind(), this));
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
			if (Status.FINISHED!=status)
			{
				_listData = null;
			}
			_contactsTask.cancel(true);
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