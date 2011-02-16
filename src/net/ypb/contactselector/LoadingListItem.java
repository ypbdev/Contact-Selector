/**
 * 
 */
package net.ypb.contactselector;

/**
 * A special class of {@link ListItem} to represent the "Loading..." list item.
 * 
 * @author anders
 *
 */
public class LoadingListItem extends ListItem
{
	public LoadingListItem()
	{
		this.contact_id = "LOADING_LIST_ITEM"; //$NON-NLS-1$
	}
}
