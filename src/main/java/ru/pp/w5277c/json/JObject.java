/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
15.04.2016	w5277c@gmail.com		Начало
13.05.2016	w5277c@gmail.com		Скопировано с основной библиотеки
21.05.2016	w5277c@gmail.com		Переделано для работы с потоком
31.07.2016	w5277c@gmail.com		Поддержка кодировки
27.03.2019	w5277c@gmail.com		Потоковая запись
12.08.2019	w5277c@gmail.com		Возможность вывода JSON с подстановкой имени ключа вместо идентификатора
11.10.2019	w5277c@gmail.com		ASCII(InputStream) и UTF-8(InputStreamReader) режим парсинга
06.12.2022	konstantin@5277.ru		Добавлен функционал экранирования
18.03.2024	w5277c@gmail.com		Не нужно экранировать одиночные кавычки
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
//TODO ВЫПИЛИТЬ работу с InputStream, так как не поддерживает UTF-8

package ru.pp.w5277c.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class JObject {
	public		static	final	String				TRUE				= "true";
	public		static	final	String				FALSE				= "false";
	public		static	final	char				T					= 't';
	public		static	final	char				F					= 'f';
	public		static	final	char				DELIMETER			= ':';
	public		static	final	char				OPEN_BRACKET		= '{';
	public		static	final	char				CLOSE_BRACKET		= '}';
	public		static	final	char				SQ_OPEN_BRACKET		= '[';
	public		static	final	char				SQ_CLOSE_BRACKET	= ']';
	public		static	final	char				COMMA				= ',';
	public		static	final	char				QUOT_MARK			= '\"';
	public		static	final	char				SPACE				= ' ';
	public		static	final	char				NEW_LINE			= '\n';
	public		static	final	String				NULL				= "null";

	protected					LinkedList<JObject>	jitems				= new LinkedList<JObject>();
	protected					Map<String, JObject>indexes				= new HashMap<String, JObject>();
	protected					String				id					= null;
	protected					String				value;

	private						long				parse_time			= 0;

	private		static			EscapeInterface		ei						= new EscapeInterface() {
		@Override
		public char escape(char l_char) {
			switch(l_char) {
				case '\t':
					return 't';
				case '\b':
					return 'b';
				case '\n':
					return 'n';
				case '\r':
					return 'r';
				case '\f':
					return 'f';
//				case '\'':
//					return '\'';
				case '\"':
					return '\"';
				case '\\':
					return '\\';
			}
			return 0x00;
		}

		@Override
		public char unescape(char l_char) {
			switch(l_char) {
				case 't':
					return '\t';
				case 'b':
					return '\b';
				case 'n':
					return '\n';
				case 'r':
					return '\r';
				case 'f':
					return '\f';
//				case '\'':
//					return '\'';
				case '\"':
					return '\"';
				case '\\':
					return '\\';
			}
			return l_char;
		}
	};
			  
	
	public JObject() {
	}

	public JObject(String l_id) {
		id = l_id;
	}

	public static InputStream str2stream(String l_str) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(l_str.getBytes("UTF-8"));
	}

	public JObject(InputStream l_is) throws Exception {
		char ch = skip(l_is, SPACE);
		if(OPEN_BRACKET != ch) {
			throw new ParseException("Missing open bracket, got:" + Short2Hex(ch) + ":'" + ch + "'");
		}
		long tmp = System.currentTimeMillis();
		parse(l_is);
		parse_time = System.currentTimeMillis() - tmp;
	}

	public JObject(InputStreamReader l_isr) throws Exception {
		char ch = skip(l_isr, SPACE);
		if(65535 == ch) {
			throw new UnexpectedEndException();
		}
		else if(OPEN_BRACKET != ch) {
			throw new ParseException("Missing open bracket, got:" + Short2Hex(ch) + ":'" + ch + "'");
		}
		long tmp = System.currentTimeMillis();
		parse(l_isr);
		parse_time = System.currentTimeMillis() - tmp;
	}

	public boolean add(JObject ...l_jobjects) {
		boolean changed = false;
		for(JObject l_jobj : l_jobjects) {
			changed |= jitems.add(l_jobj);
			if(null != l_jobj.get_id()) {
				indexes.put(l_jobj.get_id(), l_jobj);
			}
		}
		return changed;
	}

	protected void parse(InputStreamReader l_isr) throws Exception {
		while(true) {
			char ch = skip(l_isr, SPACE);
			if(ch == NEW_LINE) {
				ch = skip(l_isr, NEW_LINE);
			}
			String sub_object_id = null;
			if(QUOT_MARK == ch) {
				StringBuilder sb = new StringBuilder();
				read_str(l_isr, sb);
				sub_object_id = sb.toString();
				ch = skip(l_isr, SPACE);
				if(DELIMETER != ch) {
					throw new ParseException("Missing delimeter");
				}
				ch = skip(l_isr, SPACE);
			}
			if(ch == NEW_LINE) {
				ch = skip(l_isr, NEW_LINE);
			}

			JObject jobj = null;
			switch(ch) {
				case CLOSE_BRACKET:
				case SQ_CLOSE_BRACKET:
					break;
				case OPEN_BRACKET:
					jobj = new JObject(sub_object_id);
					((JObject)jobj).parse(l_isr);
					ch = (char)l_isr.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case SQ_OPEN_BRACKET:
					jobj = new JArray(sub_object_id);
					((JArray)jobj).parse(l_isr);
					ch = (char)l_isr.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case QUOT_MARK:
					jobj = new JString(sub_object_id);
					((JString)jobj).parse(l_isr);
					ch = (char)l_isr.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case T:
				case F:
					jobj = new JBoolean(sub_object_id);
					ch = ((JBoolean)jobj).parse(l_isr, ch);
					break;
				default:
					jobj = new JNumber(sub_object_id);
					ch = ((JNumber)jobj).parse(l_isr, ch);
					break;
			}
			if(null != jobj) {
				jitems.add(jobj);
				if(null != jobj.get_id()) {
					indexes.put(jobj.get_id(), jobj);
				}
			}

			if(ch == NEW_LINE) {
				ch = skip(l_isr, NEW_LINE);
			}

			if(this instanceof JArray && SQ_CLOSE_BRACKET == ch) {
				break;
			}
			if(this instanceof JObject && CLOSE_BRACKET == ch) {
				break;
			}
			if(COMMA != ch) {
				throw new ParseException("Expected comma");
			}
		}
	}
	protected void parse(InputStream l_is) throws Exception {
		while(true) {
			char ch = skip(l_is, SPACE);
			if(ch == NEW_LINE) {
				ch = skip(l_is, NEW_LINE);
			}
			String sub_object_id = null;
			if(QUOT_MARK == ch) {
				StringBuilder sb = new StringBuilder();
				read_str(l_is, sb);
				sub_object_id = sb.toString();
				ch = skip(l_is, SPACE);
				if(DELIMETER != ch) {
					throw new ParseException("Missing delimeter");
				}
				ch = skip(l_is, SPACE);
			}
			if(ch == NEW_LINE) {
				ch = skip(l_is, NEW_LINE);
			}

			JObject jobj = null;
			switch(ch) {
				case CLOSE_BRACKET:
				case SQ_CLOSE_BRACKET:
					break;
				case OPEN_BRACKET:
					jobj = new JObject(sub_object_id);
					((JObject)jobj).parse(l_is);
					ch = (char)l_is.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case SQ_OPEN_BRACKET:
					jobj = new JArray(sub_object_id);
					((JArray)jobj).parse(l_is);
					ch = (char)l_is.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case QUOT_MARK:
					jobj = new JString(sub_object_id);
					((JString)jobj).parse(l_is);
					ch = (char)l_is.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case T:
				case F:
					jobj = new JBoolean(sub_object_id);
					ch = ((JBoolean)jobj).parse(l_is, ch);
					break;
				default:
					jobj = new JNumber(sub_object_id);
					ch = ((JNumber)jobj).parse(l_is, ch);
					break;
			}
			if(null != jobj) {
				jitems.add(jobj);
				if(null != jobj.get_id()) {
					indexes.put(jobj.get_id(), jobj);
				}
			}

			if(ch == NEW_LINE) {
				ch = skip(l_is, NEW_LINE);
			}

			if(this instanceof JArray && SQ_CLOSE_BRACKET == ch) {
				break;
			}
			if(this instanceof JObject && CLOSE_BRACKET == ch) {
				break;
			}
			if(COMMA != ch) {
				throw new ParseException("Expected comma");
			}
		}
	}

	public String get_id() {
		return id;
	}
	public void set_id(String l_id) {
		id = l_id;
	}

	public String get_value() {
		return value;
	}
	public void set_value(String l_value) throws Exception {
		value = l_value;
	}

	public boolean contains(String l_id) {
		return indexes.containsKey(l_id);
	}

	public JObject get_object(String l_id) throws MissingFieldException {
		JObject obj = indexes.get(l_id);
		if(null == obj) {
			throw new MissingFieldException(l_id);
		}
		return obj;
	}

	public Long get_long(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return NULL.equals(obj.get_value()) ? null : Long.parseLong(obj.get_value());
	}
	public Integer get_int(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return NULL.equals(obj.get_value()) ? null : Integer.parseInt(obj.get_value());
	}
	public Double get_double(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return NULL.equals(obj.get_value()) ? null : Double.parseDouble(obj.get_value());
	}
	public Float get_float(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return NULL.equals(obj.get_value()) ? null : Float.parseFloat(obj.get_value());
	}
	public Byte get_byte(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return NULL.equals(obj.get_value()) ? null : Byte.parseByte(obj.get_value());
	}
	public Boolean get_boolean(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		if(obj.get_value().equalsIgnoreCase(TRUE)) {
			return true;
		}
		else if (obj.get_value().equalsIgnoreCase(FALSE)) {
			return false;
		}
		return null;
	}
	public String get_string(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return obj.get_value();
	}
	public byte[] get_bytes(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return (null == obj.get_value() || NULL.equals(obj.get_value()) || obj.get_value().isEmpty()) ? null : Utils.parseHexBinary(obj.get_value());
	}
	public byte[] get_base64(String l_id) throws MissingFieldException {
		JObject obj = get_object(l_id);
		return (null == obj.get_value() || NULL.equals(obj.get_value()) || obj.get_value().isEmpty()) ? null : Utils.parseBase64Binary(obj.get_value());
	}

	public LinkedList<JObject> get_items() {
		return jitems;
	}

	char skip(InputStreamReader l_isr, char l_ch) throws Exception {
		char ch;
		while(true) {
			ch = (char)l_isr.read();
			if(65535 == ch) {
				throw new UnexpectedEndException();
			}
			if(l_ch != ch) {
				break;
			}
		}
		return ch;
	}
	char skip(InputStream l_is, char l_ch) throws Exception {
		int tmp;
		while((tmp = l_is.read()) != -1) {
			char ch = (char)tmp;
			if(l_ch != ch) {
				return ch;
			}
		}
		throw new UnexpectedEndException();
	}

	protected char read(InputStreamReader l_isr, StringBuilder l_sb, char... l_stop_chs) throws Exception {
		char ch;
l1:
		while(true) {
			ch = (char)l_isr.read();
			if(65535 == ch) {
				throw new UnexpectedEndException();
			}
			for(char stop_ch : l_stop_chs) {
				if(stop_ch == ch) {
					break l1;
				}
			}
			l_sb.append(ch);
		}
		return ch;
	}
	protected char read(InputStream l_is, StringBuilder l_sb, char... l_stop_chs) throws Exception {
		int tmp;
		while((tmp = l_is.read()) != -1) {
			char ch = (char)tmp;
			for(char stop_ch : l_stop_chs) {
				if(stop_ch == ch) {
					return ch;
				}
			}
			l_sb.append(ch);
		}
		throw new UnexpectedEndException();
	}

	protected char read_str(InputStreamReader l_isr, StringBuilder l_sb) throws Exception {
		char last_ch = 0x00;
		char ch;
		while(true) {
			ch = (char)l_isr.read();
			if(65535 == ch) {
				throw new UnexpectedEndException();
			}
			if(null != ei) {
				if('\\' == last_ch) {
					if('u' == ch) {
						ch = (char)Long.parseLong("" + (char)l_isr.read() + (char)l_isr.read() + (char)l_isr.read() + (char)l_isr.read(), 0x10);
					}
					else {
						ch = ei.unescape(ch);
					}
					last_ch = ch;
				   l_sb.append(ch);
					continue;
				}
				else if('\\' == ch) {
					last_ch = ch;
					continue;
				}
			}				
			if(QUOT_MARK == ch) {
				break;
			}
			last_ch = ch;
			l_sb.append(ch);
		}
		return ch;
	}
	protected char read_str(InputStream l_is, StringBuilder l_sb) throws Exception {
		char last_ch = 0x00;
		int tmp;
		while((tmp = l_is.read()) != -1) {
			char ch = (char)tmp;
			if(null != ei) {
				if('\\' == last_ch) {
					if('u' == ch) {
						ch = (char)Long.parseLong("" + (char)l_is.read() + (char)l_is.read() + (char)l_is.read() + (char)l_is.read(), 0x10);
					}
					else {
						ch = ei.unescape(ch);
					}
					last_ch = ch;
					l_sb.append(ch);
					continue;
				}
				else if('\\' == ch) {
					last_ch = ch;
					continue;
				}
			}				
			if(QUOT_MARK == ch) {
				return ch;
			}
			last_ch = ch;
			l_sb.append(ch);
		}
		throw new UnexpectedEndException();
	}

	public static String Short2Hex(int l_val) {
		final String result = Integer.toHexString(l_val & 0x0000ffff);
		return "0000".substring(0x00, 0x04 - result.length()) + result;
	}

	public String toString(String l_value) {
		return (null == id ? l_value : QUOT_MARK + escape(id) + QUOT_MARK + DELIMETER + l_value);
	}

	void write_left(OutputStream l_os) throws IOException {
		if(null != id) {
			StringBuilder sb = new StringBuilder().append(QUOT_MARK).append(escape(id)).append(QUOT_MARK).append(DELIMETER);
			l_os.write(sb.toString().getBytes("UTF-8"));
		}
	}
	void write_right(OutputStream l_os) throws IOException {
	}

	public void write(OutputStream l_os) throws IOException {
		if(null != value) {
			write_left(l_os);
			l_os.write(escape(value).getBytes("UTF-8"));
			write_right(l_os);
		}
		else {
			if(null != id) {
				StringBuilder sb = new StringBuilder().append(QUOT_MARK).append(escape(id)).append(QUOT_MARK).append(DELIMETER);
				l_os.write(sb.toString().getBytes("UTF-8"));
			}
			l_os.write(OPEN_BRACKET);
			if(!jitems.isEmpty()) {
				for(JObject jobj : jitems) {
					jobj.write(l_os);
					if(jitems.getLast() != jobj) {
						l_os.write(COMMA);
					}
				}
			}
			l_os.write(CLOSE_BRACKET);
		}
	}

	protected String escape(String l_str) {
		if(null == ei || null == l_str) {
			return l_str;
		}
		
		StringBuilder sb = new StringBuilder();
		for(int pos=0; pos < l_str.length(); pos++) {
			char ch = l_str.charAt(pos);
			char ech = ei.escape(ch);
			if(0x00 == ech) {
				sb.append(ch);
			}
			else {
				sb.append("\\").append(ech);
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		if(null != value) {
			return toString(value);
		}
		else {
			StringBuilder sb = new StringBuilder(null == id ? "" : QUOT_MARK + escape(id) + QUOT_MARK + "" + DELIMETER);
			sb.append(OPEN_BRACKET);
			if(!jitems.isEmpty()) {
				for(JObject jobj : jitems) {
				sb.append(jobj.toString());
				sb.append(COMMA);
				}
				sb.deleteCharAt(sb.length() - 0x01);
			}
			sb.append(CLOSE_BRACKET);
			return sb.toString();
		}
	}

	public long get_parse_time() {
		return parse_time;
	}

	public static void set_ei(EscapeInterface l_ei) {
		ei = l_ei;
	}
}
