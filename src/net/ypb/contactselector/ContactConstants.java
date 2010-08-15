/**
 * 
 */
package net.ypb.contactselector;

/**
 * Constants for this application.
 * 
 * @author Anders
 *
 */
public interface ContactConstants
{
	// data filled by ContactAccessor
	public static final String INFO_SEPARATOR = " : ";
	public static final String PACKAGE = "net.ypb.contactselector";

	// keys of map returned by ContactAccessor
	public static final String NAME = "Name";
	public static final String CONTACT_ID = "ContactId";
	public static final String INFO = "Info";
	
	// display data
	public static final String INFO_OPEN = "<";
	public static final String INFO_CLOSE = ">";

	// HTML code for web-based activities
	public static final String HTML_BODY_PRE = "<html><body bgcolor=\"black\" style=\"color:white\">";
	public static final String HTML_BODY_POST = "</body></html>";
}
