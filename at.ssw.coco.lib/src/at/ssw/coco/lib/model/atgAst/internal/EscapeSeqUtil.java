/*******************************************************************************
 * Copyright (C) 2011 Martin Preinfalk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *******************************************************************************/

package at.ssw.coco.lib.model.atgAst.internal;

/** 
 * Utility Class for Unescaping Strings	
 * exported from Coco/R Source Code to be as conform as possible with Coco/R
 *
 * @author Martin Preinfalk <martin.preinfalk@students.jku.at>
 *
 */
public class EscapeSeqUtil {
	 
	/**
	 * converts a hex number in a string into a char
	 * 
	 * @param s
	 * @return character
	 * @throws Exception when finding bad escape sequence in string or character
	 */
	private static char Hex2Char(String s) throws Exception {
		int val = 0;
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ('0' <= ch && ch <= '9') 
				val = 16 * val + (ch - '0');
			else if ('a' <= ch && ch <= 'f') 
				val = 16 * val + (10 + ch - 'a');
			else if ('A' <= ch && ch <= 'F') 
				val = 16 * val + (10 + ch - 'A');
			else
				throw new Exception("bad escape sequence in string or character");
		}
		if (val > Character.MAX_VALUE) /* pdt */
			throw new Exception("bad escape sequence in string or character");
		return (char) val;
	}

	/**
	 * replaces escape sequences in s by their Unicode values.
	 * 
	 * @param s String to be unescaped
	 * @return unescaped string
	 * @throws Exception when finding bad escape sequence in string or character
	 */
	public static String unescape (String s) throws Exception {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		while (i < s.length()) {
			if (s.charAt(i) == '\\') {
				switch (s.charAt(i+1)) {
		        	case '\\': 
		        		buf.append('\\'); i += 2; 
		        		break;
		        	case '\'': 
		        		buf.append('\''); i += 2; 
		        		break;
		        	case '\"': 
		        		buf.append('\"'); i += 2; 
		        		break;		          
		        	case 'r': 
		        		buf.append('\r'); i += 2; 
		        		break;		          
		        	case 'n': 
		        		buf.append('\n'); i += 2; 
		        		break;		          
		        	case 't': 
		        		buf.append('\t'); i += 2; 
		        		break;		          
		        	case 'v': 
		        		buf.append('\u000b'); i += 2; 
		        		break;		          
		        	case '0': 
		        		buf.append('\0'); i += 2; 
		        		break;		          
		        	case 'b': 
		        		buf.append('\b'); i += 2; 
		        		break;		          
		        	case 'f': 
		        		buf.append('\f'); i += 2; 
		        		break;		          
		        	case 'a': 
		        		buf.append('\u0007'); i += 2; 
		        		break;		          
		        	case 'u': 
		        	case 'x':
		        		if (i + 6 <= s.length()) {		              
		        			buf.append(Hex2Char(s.substring(i+2, i+6))); i += 6; 
		        			break;		            
		        		} else {		              
		        			throw new Exception("bad escape sequence in string or character");		              
		        		}		          
		        	default: 
		        		throw new Exception("bad escape sequence in string or character");
				}		      
			} else {		        
				buf.append(s.charAt(i));
		        i++;	
			}		    
		}
		return buf.toString();		
	}
}
