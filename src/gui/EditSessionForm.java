/*
 * Created on Oct 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import app.SessionManager;
import app.SessionSpec;

/**
 * @author Karl
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class EditSessionForm extends ConnectionForm {

	private static Command saveCommand = new Command( "Save", Command.SCREEN, 1 );

	private int connectionIndex = 1;

	/**
	 * @param back
	 * @param title
	 */
	public EditSessionForm() {
		super( "Edit Session" );

		addCommand( saveCommand );
	}

	public void setConnectionIndex( int connectionIndex ) {
		this.connectionIndex = connectionIndex;

		SessionSpec conn = SessionManager.getSession( connectionIndex );
		if ( conn != null ) {
			tfAlias.setString( conn.alias );
			tfHost.setString( conn.host );
			tfUsername.setString( conn.username );
			tfPassword.setString( conn.password );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command,
	 *      javax.microedition.lcdui.Displayable)
	 */
	public void commandAction( Command command, Displayable displayed ) {
		if ( command == saveCommand ) {
			doSave();
		}
		else {
			super.commandAction( command, displayed );
		}
	}

	private void doSave() {
		if ( connectionIndex != -1 ) {
			if ( validateForm() ) {
				String alias = tfAlias.getString();
				String type = selectedConnectionType();
				String host = tfHost.getString();
				String username = tfUsername.getString();
				String password = tfPassword.getString();

				SessionSpec conn = new SessionSpec();
				conn.alias = alias;
				conn.type = type;
				conn.host = host;
				conn.username = username;
				conn.password = password;
				SessionManager.replaceSession( connectionIndex, conn );

				doBack();
			}
		}
	}

}