/* This file is part of "MidpSSH".
 * Copyright (c) 2004 Karl von Randow.
 * 
 * MidpSSH is based upon Telnet Floyd and FloydSSH by Radek Polak.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 *
 */
package gui.session;

import gui.Activatable;
import gui.MessageForm;
import gui.session.macros.MacroSetsMenu;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import terminal.KeyEvent;
import terminal.Terminal;
import terminal.vt320;
import app.Main;
import app.Settings;
import app.SettingsManager;
import app.session.Session;

/**
 * @author Karl von Randow
 * 
 */
public class SessionTerminal extends Terminal implements Activatable, CommandListener {

	private static final int MODE_DISCONNECTED = 0;
	
	private static final int MODE_CONNECTED = 1;

	private static final int MODE_CURSOR = 2;

	private static final int MODE_SCROLL = 3;
	
	private static int commandPriority = 1;

	private static final Command textInputCommand = new Command( "Input", Command.ITEM, commandPriority++ );

	private static final Command macrosCommand = new Command( "Macros", Command.ITEM, commandPriority++ );

	private static final Command tabCommand = new Command( "TAB", Command.ITEM, commandPriority++ );

	private static final Command spaceCommand = new Command( "SPACE", Command.ITEM, commandPriority++ );

	private static final Command enterCommand = new Command( "ENTER", Command.ITEM, commandPriority++ );

	private static final Command escCommand = new Command( "ESC", Command.ITEM, commandPriority++ );

	private static final Command backspaceCommand = new Command( "BACKSPACE", Command.ITEM, commandPriority++ );

	private static final Command ctrlCommand = new Command( "CTRL", Command.ITEM, commandPriority++ );

	private static final Command altCommand = new Command( "ALT", Command.ITEM, commandPriority++ );

	private static final Command cursorCommand = new Command( "Cursor", Command.ITEM, commandPriority++ );

	private static final Command scrollCommand = new Command( "Scroll", Command.ITEM, commandPriority++ );

	private static final Command backCommand = new Command( "Back", Command.BACK, commandPriority++ );
	
	private static final Command showBindingsCommand = new Command( "Show Key Bindings", Command.ITEM, commandPriority++ );

	//private static final Command settingsCommand = new Command( "Settings", Command.ITEM, commandPriority++ );

	private static final Command disconnectCommand = new Command( "Close", Command.STOP, commandPriority++ );

	private static final Command[] commandsDisconnected = new Command[] {
			disconnectCommand
	};
	
	private static final Command[] commandsConnected = new Command[] {
			textInputCommand, macrosCommand, 
			tabCommand, spaceCommand, enterCommand, escCommand, backspaceCommand,
			ctrlCommand, altCommand,
			cursorCommand, scrollCommand,
			showBindingsCommand,
			//settingsCommand,
			disconnectCommand
	};

	private static final Command[] commandsCursor = new Command[] {
		backCommand
	};
	
	private static final int [] bindingKeys = new int[] {
			Canvas.KEY_NUM1, Canvas.KEY_NUM2, Canvas.KEY_NUM3,
			Canvas.KEY_NUM4, Canvas.KEY_NUM5, Canvas.KEY_NUM6,
			Canvas.KEY_NUM7, Canvas.KEY_NUM8, Canvas.KEY_NUM9,
			Canvas.KEY_STAR, Canvas.KEY_NUM0, Canvas.KEY_POUND
	};

	private Session session;

	private static InputDialog inputDialog;
	
	private static MacroSetsMenu macrosMenu;
	
	//private static SessionSettingsMenu settingsMenu;

	private ModifierInputDialog controlKeyDialog, altKeyDialog;

	private Command[] currentCommands;

	private int mode;

	/**
	 * @param buffer
	 */
	public SessionTerminal( vt320 buffer, Session session ) {
		super( buffer );
		this.session = session;

		changeMode( MODE_DISCONNECTED );

		setCommandListener( this );
		
		// Settings
		Settings settings = SettingsManager.getSettings();
		
		if ( Main.useColors ) {
			bgcolor = settings.bgcolor;
			fgcolor = settings.fgcolor;
		}
		
		boolean resized = false;
		int cols = this.cols;
		int rows = this.rows;
		if ( settings.screenColumns != 0 ) {
			cols = settings.screenColumns;
			resized = true;
		}
		if ( settings.screenRows != 0 ) {
			rows = settings.screenRows;
			resized = true;
		}
		if ( resized ) {
			buffer.setScreenSize( cols, rows );
		}
	}
	
	public void connected() {
		changeMode( MODE_CONNECTED );
	}
	
	public void disconnected() {
		changeMode( MODE_DISCONNECTED );
	}

	protected void changeMode( int mode ) {
		this.mode = mode;

		switch ( mode ) {
			case MODE_DISCONNECTED:
				changeCurrentCommands( commandsDisconnected );
				break;
			case MODE_CONNECTED:
				changeCurrentCommands( commandsConnected );
				break;
			case MODE_CURSOR:
			case MODE_SCROLL:
				changeCurrentCommands( commandsCursor );
				break;
		}
	}

	protected void changeCurrentCommands( Command[] commands ) {
		if ( currentCommands != null ) {
			for ( int i = 0; i < currentCommands.length; i++ ) {
				removeCommand( currentCommands[i] );
			}
		}

		for ( int i = 0; i < commands.length; i++ ) {
			addCommand( commands[i] );
		}

		this.currentCommands = commands;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gui.Activatable#activate()
	 */
	public void activate() {
		Main.setDisplay( this );
	}
	
	public void activate( Activatable back ) {
		activate();
	}

	public void commandAction( Command command, Displayable displayable ) {
		if ( command == disconnectCommand ) {
			doDisconnect();
		}
		else if ( command == textInputCommand ) {
			doTextInput();
		}
		else if ( command == macrosCommand ) {
//#ifndef nomacros
			doMacros();
//#endif
		}
		else if ( command == tabCommand ) {
			buffer.keyTyped( 0, '\t', 0 );
		}
		else if ( command == spaceCommand ) {
			buffer.keyTyped( 0, ' ', 0 );
		}
		else if ( command == enterCommand ) {
			buffer.keyTyped( 0, '\n', 0 );
		}
		else if ( command == escCommand ) {
			buffer.keyTyped( 0, (char) 27, 0 );
		}
		else if ( command == backspaceCommand ) {
			buffer.keyPressed( 8, '\b', 0 );
		}
		else if ( command == ctrlCommand ) {
			doControlKeyInput();
		}
		else if ( command == altCommand ) {
			doAltKeyInput();
		}
		else if ( command == cursorCommand ) {
			doCursor();
		}
		else if ( command == scrollCommand ) {
			doScroll();
		}
		else if ( command == backCommand ) {
			changeMode( MODE_CONNECTED );
		}
		else if ( command == showBindingsCommand ) {
			doShowBindings();
		}
		/*else if ( command == settingsCommand ) {
			doSettings();
		}*/
	}

	protected void keyPressed( int keycode ) {
		switch ( mode ) {
			case MODE_CONNECTED:
				keyPressedConnected( keycode );
				break;
			case MODE_CURSOR:
				keyPressedCursor( keycode );
				break;
			case MODE_SCROLL:
				keyPressedScroll( keycode );
				break;
		}
	}

	protected void keyRepeated( int keycode ) {
		switch ( mode ) {
			case MODE_CURSOR:
				keyPressedCursor( keycode );
				break;
			case MODE_SCROLL:
				keyPressedScroll( keycode );
				break;
		}
	}

	protected void keyPressedConnected( int keycode ) {
		int index = -1;
		/*
		// Map keys to actions
		if ( keycode >= Canvas.KEY_NUM1 && keycode <= Canvas.KEY_NUM9 ) {
			index = keycode - Canvas.KEY_NUM1;
		}
		else {
			switch ( keycode ) {
				case Canvas.KEY_STAR:
					index = 10;
					break;
				case Canvas.KEY_NUM0:
					index = 11;
					break;
				case Canvas.KEY_POUND:
					index = 12;
					break;
			}
		}*/
		
		for ( int i = 0; i < bindingKeys.length; i++ ) {
			if ( bindingKeys[i] == keycode ) {
				index = i;
				break;
			}
		}
		
		if ( index >= 0 && index < commandsConnected.length ) {
			commandAction( commandsConnected[index], this );
		}
	}
	
	private int gameKeysToNumeric( int keycode ) {
		// Convert game actions to keys
		int gameAction = getGameAction( keycode );
		switch ( gameAction ) {
			case Canvas.UP:
				keycode = Canvas.KEY_NUM2;
				break;
			case Canvas.DOWN:
				keycode = Canvas.KEY_NUM8;
				break;
			case Canvas.LEFT:
				keycode = Canvas.KEY_NUM4;
				break;
			case Canvas.RIGHT:
				keycode = Canvas.KEY_NUM6;
				break;
		}
		return keycode;
	}

	protected void keyPressedCursor( int keycode ) {
		keycode = gameKeysToNumeric( keycode );
		
		switch ( keycode ) {
			case Canvas.KEY_NUM2:
				buffer.keyPressed( KeyEvent.VK_UP, (char) 65535, vt320.KEY_ACTION );
				break;
			case Canvas.KEY_NUM8:
			case Canvas.KEY_NUM0:
				buffer.keyPressed( KeyEvent.VK_DOWN, (char) 65535, vt320.KEY_ACTION );
				break;
			case Canvas.KEY_NUM4:
				buffer.keyPressed( KeyEvent.VK_LEFT, (char) 65535, vt320.KEY_ACTION );
				break;
			case Canvas.KEY_NUM6:
				buffer.keyPressed( KeyEvent.VK_RIGHT, (char) 65535, vt320.KEY_ACTION );
				break;
			case Canvas.KEY_NUM1:
				keyPressedCursor( Canvas.UP );
				keyPressedCursor( Canvas.LEFT );
				break;
			case Canvas.KEY_NUM3:
				keyPressedCursor( Canvas.UP );
				keyPressedCursor( Canvas.RIGHT );
				break;
			case Canvas.KEY_NUM7:
			case Canvas.KEY_STAR:
				keyPressedCursor( Canvas.DOWN );
				keyPressedCursor( Canvas.LEFT );
				break;
			case Canvas.KEY_NUM9:
			case Canvas.KEY_POUND:
				keyPressedCursor( Canvas.DOWN );
				keyPressedCursor( Canvas.RIGHT );
				break;
		}
	}

	protected void keyPressedScroll( int keycode ) {
		keycode = gameKeysToNumeric( keycode );
		
		switch ( keycode ) {
			case Canvas.KEY_NUM2:
				if ( top > 0 ) {
					top--;
				}
				redraw();
				break;
			case Canvas.KEY_NUM8:
			case Canvas.KEY_NUM0:
				if ( top + rows < buffer.height ) {
					top++;
				}
				redraw();
				break;
			case Canvas.KEY_NUM4:
				if ( left > 0 ) {
					left--;
				}
		        redraw();
				break;
			case Canvas.KEY_NUM6:
				if ( left + cols < buffer.width ) {
					left++;
				}
				redraw();
				break;
			case Canvas.KEY_NUM1:
				keyPressedScroll( Canvas.UP );
			keyPressedScroll( Canvas.LEFT );
				break;
			case Canvas.KEY_NUM3:
				keyPressedScroll( Canvas.UP );
			keyPressedScroll( Canvas.RIGHT );
				break;
			case Canvas.KEY_NUM7:
			case Canvas.KEY_STAR:
				keyPressedScroll( Canvas.DOWN );
				keyPressedScroll( Canvas.LEFT );
				break;
			case Canvas.KEY_NUM9:
			case Canvas.KEY_POUND:
				keyPressedScroll( Canvas.DOWN );
				keyPressedScroll( Canvas.RIGHT );
				break;
		}
	}

	private void doDisconnect() {
		session.disconnect();
		session.goMainMenu();
	}
	
	private void doTextInput() {
		if ( inputDialog == null ) {
			inputDialog = new InputDialog();
		}
		inputDialog.activate( this );
	}
	
	private void doMacros() {
		if ( macrosMenu == null ) {
			macrosMenu = new MacroSetsMenu();
		}
		macrosMenu.activate( this );
	}

	private void doControlKeyInput() {
		if ( controlKeyDialog == null ) {
			controlKeyDialog = new ModifierInputDialog( "Control Keys", vt320.KEY_CONTROL );
		}
		controlKeyDialog.activate( this );
	}

	private void doAltKeyInput() {
		if ( altKeyDialog == null ) {
			altKeyDialog = new ModifierInputDialog( "Alt Keys", vt320.KEY_ALT );
		}
		altKeyDialog.activate( this );
	}

	private void doCursor() {
		changeMode( MODE_CURSOR );
	}

	private void doScroll() {
		changeMode( MODE_SCROLL );
	}
	
	private void doShowBindings() {
		StringBuffer str = new StringBuffer();
		
		if ( currentCommands != null ) {
			for ( int i = 0; i < bindingKeys.length && i < currentCommands.length; i++ ) {
				int keycode = bindingKeys[i];
				Command comm = currentCommands[i];
				String keyName = getKeyName( keycode );
				str.append( keyName );
				str.append( ": " );
				str.append( comm.getLabel() );
				str.append( "\n" );
			}
		}
		
		new MessageForm( "Key Bindings", str.toString() ).activate( this );
	}
	
	/*private void doSettings() {
		if ( settingsMenu == null ) {
			settingsMenu = new SessionSettingsMenu();
		}
		settingsMenu.activate( this );
	}*/
}