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
package gui.settings;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import app.Settings;

/**
 * @author Karl von Randow
 */
public class TerminalSettingsForm extends SettingsForm {

	protected TextField tfType = new TextField( "Type", "", 20, TextField.ANY );
	
	protected TextField tfCols = new TextField( "Cols", "", 3, TextField.NUMERIC );
	
	protected TextField tfRows = new TextField( "Rows", "", 3, TextField.NUMERIC );
	
	protected TextField tfFg = new TextField( "Foreground", "", 6, TextField.ANY );
	
	protected TextField tfBg = new TextField( "Background", "", 6, TextField.ANY );
//#ifdef midp2    
    protected ChoiceGroup cgFullscreen = new ChoiceGroup( "Full Screen", ChoiceGroup.EXCLUSIVE );
//#endif
    protected ChoiceGroup cgFont = new ChoiceGroup( "Font Size", ChoiceGroup.EXCLUSIVE );
	
	protected ChoiceGroup cgRotated = new ChoiceGroup( "Orientation", ChoiceGroup.EXCLUSIVE );
	
	public TerminalSettingsForm() {
		super( "Terminal Settings" );

		append( new StringItem( "Type", "The terminal type reported to the remote server. The default type is VT320." ) );
		append( tfType );
		
		append( new StringItem( "Size", "The size of the terminal. The default is to use the maximum available screen area." ) );
		append( tfCols );
		append( tfRows );
		
//#ifdef midp2
        cgFullscreen.append( "Off", null );
        cgFullscreen.append( "On", null );
        append( cgFullscreen );
//#endif
        
        cgFont.append( "Tiny", null );
        cgFont.append( "Small", null );
        cgFont.append( "Medium", null );
        cgFont.append( "Large", null );
        append( cgFont );
        
		append( new StringItem( "Colour", "Enter colours in hexadecimal, eg. 6699cc" ) );
		append( tfFg );
		append( tfBg );
		
		cgRotated.append( "Normal", null );
		cgRotated.append( "Landscape", null );
        cgRotated.append( "Landscape 2", null );
//#ifdef midp2
		append( cgRotated );
//#else
		append( new StringItem( "Orientation", "Not available on this device." ) );
//#endif
	}
	/* (non-Javadoc)
	 * @see gui.Activatable#activate()
	 */
	public void activate() {
		int cols = Settings.terminalCols;
		int rows = Settings.terminalRows;
		
		tfType.setString( Settings.terminalType );
		
		if ( cols > 0 ) {
			tfCols.setString( "" + cols );
		}
		else {
			tfCols.setString( "" );
		}
		if ( rows > 0 ) {
			tfRows.setString( "" + rows );
		}
		else {
			tfRows.setString( "" );
		}
//#ifdef midp2
        cgFullscreen.setSelectedIndex( Settings.terminalFullscreen ? 1 : 0, true );
//#endif        
		tfFg.setString( toHex( Settings.fgcolor ) );
		tfBg.setString( toHex( Settings.bgcolor ) );
		
        switch ( Settings.fontMode ) {
        case Settings.FONT_NORMAL:
            cgFont.setSelectedIndex( 0, true );
            break;
        case Settings.FONT_SMALL:
            cgFont.setSelectedIndex( 1, true );
            break;
        case Settings.FONT_MEDIUM:
            cgFont.setSelectedIndex( 2, true );
            break;
        case Settings.FONT_LARGE:
            cgFont.setSelectedIndex( 3, true );
            break;
        }
        
        switch ( Settings.terminalRotated ) {
        case Settings.ROT_NORMAL:
            cgRotated.setSelectedIndex( 0, true );
            break;
        case Settings.ROT_270:
            cgRotated.setSelectedIndex( 1, true );
            break;
        case Settings.ROT_90:
            cgRotated.setSelectedIndex( 2, true );
            break;
        }
		
		
		super.activate();
	}
	
	protected boolean doSave( boolean doDefault ) {
		if ( !doDefault ) {
			Settings.terminalType = tfType.getString();
			
			try {
				Settings.terminalCols = Integer.parseInt( tfCols.getString() );
			}
			catch ( NumberFormatException e ) {
				
			}
			try {
				Settings.terminalRows = Integer.parseInt( tfRows.getString() );
			}
			catch ( NumberFormatException e ) {
				
			}
//#ifdef midp2
            Settings.terminalFullscreen = cgFullscreen.getSelectedIndex() == 1;
//#endif
			try {
				int col = fromHex( tfFg.getString() );
				Settings.fgcolor = col;
			}
			catch ( NumberFormatException e ) {
				
			}
			
			try {
				int col = fromHex( tfBg.getString() );
				Settings.bgcolor = col;
			}
			catch ( NumberFormatException e ) {
				
			}
			
            switch (cgFont.getSelectedIndex()) {
            case 0:
                Settings.fontMode = Settings.FONT_NORMAL;
                break;
            case 1:
                Settings.fontMode = Settings.FONT_SMALL;
                break;
            case 2:
                Settings.fontMode = Settings.FONT_MEDIUM;
                break;
            case 3:
                Settings.fontMode = Settings.FONT_LARGE;
                break;
            }
            switch ( cgRotated.getSelectedIndex() ) {
            case 0:
                Settings.terminalRotated = Settings.ROT_NORMAL;
                break;
            case 1:
                Settings.terminalRotated = Settings.ROT_270;
                break;
            case 2:
                Settings.terminalRotated = Settings.ROT_90;
                break;
            }
		}
		else {
			Settings.terminalType = "";
			Settings.terminalCols = 0;
			Settings.terminalRows = 0;
            Settings.terminalFullscreen = false;
			Settings.fgcolor = Settings.DEFAULT_FGCOLOR;
			Settings.bgcolor = Settings.DEFAULT_BGCOLOR;
			Settings.terminalRotated = Settings.ROT_NORMAL;
		}
		return true;
	}
	
	private static int fromHex( String hex ) throws NumberFormatException {
		hex = hex.toLowerCase();
		int total = 0;
		for ( int i = 0; i < hex.length(); i++ ) {
			total <<= 4;
			char c = hex.charAt( i );
			if ( c >= '0' && c <= '9' ) {
				total += ( c - '0' );
			}
			else if ( c >= 'a' && c <= 'f' ) {
				total += ( c - 'a' ) + 10;
			}
			else {
				throw new NumberFormatException( hex );
			}
		}
		return total;
	}
	
	private static String toHex( int i ) {
		char[] buf = new char[32];
		int charPos = 32;
		do {
		    buf[--charPos] = digits[i & 15];
		    i >>>= 4;
		//} while (i != 0);
		} while ( charPos > 26 || i != 0 );

		return new String(buf, charPos, (32 - charPos));
	}
	
    private final static char[] digits = {
    		'0' , '1' , '2' , '3' , '4' , '5' ,
    		'6' , '7' , '8' , '9' , 'a' , 'b' ,
    		'c' , 'd' , 'e' , 'f' 
    	    };
}
