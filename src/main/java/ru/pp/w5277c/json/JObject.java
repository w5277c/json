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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
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
	public		static	final	char				TAB					= '\t';
	public		static	final	char				NEW_LINE			= '\n';
	public		static	final	String				NULL				= "null";

	protected					LinkedList<JObject>	jitems				= new LinkedList<JObject>();
	protected					Map<String, JObject>indexes				= new HashMap<String, JObject>();
	protected					String				id					= null;
	protected					String				value;

	private						long				parseTime			= 0;

	private		static			EscapeInterface		ei					= new EscapeInterface() {
		@Override
		public char escape(char c) {
			switch(c) {
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
		public char unescape(char c) {
			switch(c) {
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
			return c;
		}
	};
			  
	
	public JObject() {
	}

	public JObject(String id) {
		this.id = id;
	}

	public static BufferedReader str2stream(String str) throws UnsupportedEncodingException {
		return new BufferedReader(new StringReader(str));
	}

	public JObject(BufferedReader br) throws Exception {
		char c = skip(br, SPACE, TAB, '\r', '\n');
		if(OPEN_BRACKET != c) {
			throw new ParseException("Missing open bracket, got:" + Short2Hex(c) + ":'" + c + "'");
		}
		long tmp = System.currentTimeMillis();
		parse(br);
		parseTime = System.currentTimeMillis() - tmp;
	}

	public JObject add(JObject ...jobjects) {
		for(JObject jobj : jobjects) {
			jitems.add(jobj);
			if(null != jobj.getId()) {
				indexes.put(jobj.getId(), jobj);
			}
		}
		return this;
	}
	
	public JObject add(String id, Object value) throws Exception {
		if(value instanceof String) {
			return add(new JString(id, (String)value));
		}
		if(	value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof Float ||
			value instanceof Double) {
			
			return add(new JNumber(id, value));
		}
		if( value instanceof Boolean) {
			return add(new JBoolean(id, (Boolean)value));
		}
		throw new Exception("Unsuported value type for:" + id);
	}

	protected void parse(BufferedReader br) throws Exception {
		while(true) {
			char c = skip(br, SPACE, TAB, '\r', '\n');
			String subObjectId = null;
			if(QUOT_MARK == c) {
				StringBuilder sb = new StringBuilder();
				readStr(br, sb);
				subObjectId = sb.toString();
				c = skip(br, SPACE, TAB, '\r', '\n');
				if(DELIMETER != c) {
					throw new ParseException("Missing delimeter");
				}
				c = skip(br, SPACE, TAB, '\r', '\n');
			}

			JObject jobj = null;
			switch(c) {
				case CLOSE_BRACKET:
				case SQ_CLOSE_BRACKET:
					break;
				case OPEN_BRACKET:
					jobj = new JObject(subObjectId);
					((JObject)jobj).parse(br);
					c = (char)br.read();
					if(65535 == c) {
						throw new UnexpectedEndException();
					}
					break;
				case SQ_OPEN_BRACKET:
					jobj = new JArray(subObjectId);
					((JArray)jobj).parse(br);
					c = (char)br.read();
					if(65535 == c) {
						throw new UnexpectedEndException();
					}
					break;
				case QUOT_MARK:
					jobj = new JString(subObjectId);
					((JString)jobj).parse(br);
					c = (char)br.read();
					if(65535 == c) {
						throw new UnexpectedEndException();
					}
					break;
				case T:
				case F:
					jobj = new JBoolean(subObjectId);
					c = ((JBoolean)jobj).parse(br, c);
					break;
				default:
					jobj = new JNumber(subObjectId);
					c = ((JNumber)jobj).parse(br, c);
					break;
			}
			if(null != jobj) {
				jitems.add(jobj);
				if(null != jobj.getId()) {
					indexes.put(jobj.getId(), jobj);
				}
			}

			if(c == SPACE || c == TAB || c == '\r' || c == '\n') {
				c = skip(br, SPACE, TAB, '\r', '\n');
			}

			if(this instanceof JArray && SQ_CLOSE_BRACKET == c) {
				break;
			}
			if(this instanceof JObject && CLOSE_BRACKET == c) {
				break;
			}
			if(COMMA != c) {
				throw new ParseException("Expected comma, got:" + c);
			}
		}
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) throws Exception {
		this.value = value;
	}

	public boolean contains(String id) {
		return indexes.containsKey(id);
	}

	public JObject getObject(String id) throws MissingFieldException {
		JObject obj = indexes.get(id);
		if(null == obj) {
			throw new MissingFieldException(id);
		}
		return obj;
	}

	public Long getLong(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return NULL.equals(obj.getValue()) ? null : Long.parseLong(obj.getValue());
	}
	public Integer getInt(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return NULL.equals(obj.getValue()) ? null : Integer.parseInt(obj.getValue());
	}
	public Double getDouble(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return NULL.equals(obj.getValue()) ? null : Double.parseDouble(obj.getValue());
	}
	public Float getFloat(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return NULL.equals(obj.getValue()) ? null : Float.parseFloat(obj.getValue());
	}
	public Byte getByte(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return NULL.equals(obj.getValue()) ? null : Byte.parseByte(obj.getValue());
	}
	public Boolean getBoolean(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		if(obj.getValue().equalsIgnoreCase(TRUE)) {
			return true;
		}
		else if (obj.getValue().equalsIgnoreCase(FALSE)) {
			return false;
		}
		return null;
	}
	public String getString(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return obj.getValue();
	}
	public byte[] getBytes(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return (null == obj.getValue() || NULL.equals(obj.getValue()) || obj.getValue().isEmpty()) ? null : Utils.parseHexBinary(obj.getValue());
	}
	public byte[] getBase64(String id) throws MissingFieldException {
		JObject obj = getObject(id);
		return (null == obj.getValue() || NULL.equals(obj.getValue()) || obj.getValue().isEmpty()) ? null : Utils.parseBase64Binary(obj.getValue());
	}

	public LinkedList<JObject> getItems() {
		return jitems;
	}

	char skip(BufferedReader br, char... chars) throws Exception {
		int tmp;
		while((tmp = br.read()) != -1) {
			char _c = (char)tmp;
			boolean exist = false;
			for(char c : chars) {
				if(c == _c) {
					exist = true;
					break;
				}
			}
			if(!exist) {
				return _c;
			}
		}
		throw new UnexpectedEndException();
	}

	protected char read(BufferedReader br, StringBuilder sb, char... stopChs) throws Exception {
		int tmp;
		while((tmp = br.read()) != -1) {
			char c = (char)tmp;
			for(char stopCh : stopChs) {
				if(stopCh == c) {
					return c;
				}
			}
			sb.append(c);
		}
		throw new UnexpectedEndException();
	}

	protected char readStr(BufferedReader br, StringBuilder sb) throws Exception {
		char lastCh = 0x00;
		int tmp;
		while((tmp = br.read()) != -1) {
			char c = (char)tmp;
			if(null != ei) {
				if('\\' == lastCh) {
					if('u' == c) {
						c = (char)Long.parseLong("" + (char)br.read() + (char)br.read() + (char)br.read() + (char)br.read(), 0x10);
					}
					else {
						c = ei.unescape(c);
					}
					lastCh = c;
					sb.append(c);
					continue;
				}
				else if('\\' == c) {
					lastCh = c;
					continue;
				}
			}				
			if(QUOT_MARK == c) {
				return c;
			}
			lastCh = c;
			sb.append(c);
		}
		throw new UnexpectedEndException();
	}

	public static String Short2Hex(int val) {
		final String result = Integer.toHexString(val & 0x0000ffff);
		return "0000".substring(0x00, 0x04 - result.length()) + result;
	}

	public String toString(String value) {
		return (null == id ? value : QUOT_MARK + escape(id) + QUOT_MARK + DELIMETER + value);
	}

	void writePrefix(OutputStream os) throws IOException {
		if(null != id) {
			StringBuilder sb = new StringBuilder().append(QUOT_MARK).append(escape(id)).append(QUOT_MARK).append(DELIMETER);
			os.write(sb.toString().getBytes("UTF-8"));
		}
	}
	void writePostfix(OutputStream os) throws IOException {
	}

	public void write(OutputStream os) throws IOException {
		if(null != value) {
			writePrefix(os);
			os.write(escape(value).getBytes("UTF-8"));
			writePostfix(os);
		}
		else {
			if(null != id) {
				StringBuilder sb = new StringBuilder().append(QUOT_MARK).append(escape(id)).append(QUOT_MARK).append(DELIMETER);
				os.write(sb.toString().getBytes("UTF-8"));
			}
			os.write(OPEN_BRACKET);
			if(!jitems.isEmpty()) {
				for(JObject jobj : jitems) {
					jobj.write(os);
					if(jitems.getLast() != jobj) {
						os.write(COMMA);
					}
				}
			}
			os.write(CLOSE_BRACKET);
		}
	}

	protected String escape(String str) {
		if(null == ei || null == str) {
			return str;
		}
		
		StringBuilder sb = new StringBuilder();
		for(int pos=0; pos < str.length(); pos++) {
			char ch = str.charAt(pos);
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

	public long getParseTime() {
		return parseTime;
	}

	public static void setEI(EscapeInterface _ei) {
		ei = _ei;
	}
}
