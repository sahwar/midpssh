/*
 * Created on Nov 25, 2004
 *
 */
package gui.session;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import terminal.KeyEvent;

import app.Main;
import app.session.Session;

import gui.Activatable;
import gui.ExtendedList;

/**
 * @author Karl
 *
 */
public class SpecialMenu extends ExtendedList implements CommandListener, Activatable {

	//private static Command selectCommand = new Command( "Select", Command.ITEM, 1 );

	private static Command backCommand = new Command( "Back", Command.BACK, 2 );

    private static final String[] MAIN_OPTIONS = new String[] {
//#ifndef nocursororscroll
        "Cursor", "Scroll",
//#endif
//#ifndef small        
        "BACKSPACE", "Page Up", "Page Down",
        "Function Keys", 
        "|", "\\", "~", ":", ";", "'", "\"",
        ",", "<", ".", ">", "/", "?",
        "`", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")",
        "-", "_", "+", "=",
        "[", "{", "]", "}"
//#endif
    };
    
//#ifndef small
    private static final String[] FUNCTION_KEY_OPTIONS = new String[] {
        "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12"
    };
    
    private SpecialMenu menuFunctionKeys;

//#endif
    
    private Activatable back, done;
    
    /**
     * @param title
     * @param mode
     */
    public SpecialMenu() {
        this( "Special Keys", MAIN_OPTIONS );
    }
    
    public SpecialMenu( String title, String [] options ) {
        super(title, List.IMPLICIT);
        
        for ( int i = 0; i < options.length; i++ ) {
            append( options[i], null );
        }

		//setSelectCommand( selectCommand );
		addCommand( backCommand );
		
		setCommandListener( this );
    }
    
    public void commandAction( Command command, Displayable displayed ) {
		if ( command == List.SELECT_COMMAND ) {
	        Session session = Main.currentSession();
			if ( session != null ) {
			    String option = getString( getSelectedIndex() );
			    int keyCode = 0;
			    String str = null;
			    
			    // Main options
			    int i = find( MAIN_OPTIONS, option );
			    if ( i != -1 ) {
			        switch ( i ) {
//#ifndef nocursororscroll
					    case 0:
					        ((SessionTerminal)done).doCursor();
					        done.activate();
					        break;
					    case 1:
					        ((SessionTerminal)done).doScroll();
					        done.activate();
					        break;
//#endif
//#ifndef small
			            case 2:
			                keyCode = KeyEvent.VK_BACK_SPACE;
			                break;
                        case 3:
                            keyCode = KeyEvent.VK_PAGE_UP;
                            break;
                        case 4:
                            keyCode = KeyEvent.VK_PAGE_DOWN;
                            break;
			            case 5:
			                if ( menuFunctionKeys == null ) {
					            menuFunctionKeys = new SpecialMenu( "Function Keys", FUNCTION_KEY_OPTIONS );
					        }
					        menuFunctionKeys.activate( this, done );
					        break;
			            default:
			                str = option;
			            	break;
//#endif
			        }
			    }
			    
//#ifndef small
			    // Function keys
			    i = find( FUNCTION_KEY_OPTIONS, option );
			    if ( i != -1 ) {
			        keyCode = KeyEvent.VK_F1 + i;
			    }
//#endif
			    if ( keyCode != 0 ) {
			        session.typeKey( keyCode, 0 );
					done.activate();
			    }
			    else if ( str != null ) {
			        session.typeString( str );
					done.activate();
			    }
		    }
		}
		else if ( command == backCommand ) {
		    if ( back != null ) {
		        back.activate();
		    }
		}
	}
    
    private int find( String [] options, String option ) {
        for (int i = 0; i < options.length; i++) {
            if ( options[i].equals( option ) ) {
                return i;
            }
        }
        return -1;
    }
    public void activate() {
        Main.setDisplay( this );
    }
    public void activate(Activatable back) {
        activate( back, back );
    }
    public void activate(Activatable back, Activatable done) {
        this.back = back;
        this.done = done;
        activate();
    }
}
