/**
 * 
 */
package net.ypb.contactselector;

/**
 * This class represents an item in the contacts list, i.e. name, number, etc.
 * 
 * @author anders
 *
 */
public class ListItem
{
	public String name = ""; //$NON-NLS-1$
	public String info = ""; //$NON-NLS-1$
	public String contact_id = ""; //$NON-NLS-1$
	
	public ListItem() {}
	
	public ListItem(String name, String info, String contact_id)
	{
		if (name!=null)
			this.name = name;
		if (info!=null)
			this.info = info;
		if (contact_id!=null)
			this.contact_id = contact_id;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ListItem)
		{
			ListItem item = (ListItem)o;
			return (item.contact_id.equals(contact_id) &&
						item.info.equals(info));
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return info.hashCode()+contact_id.hashCode();
	}
}
