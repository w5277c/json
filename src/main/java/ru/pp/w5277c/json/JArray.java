/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
15.04.2016	w5277c@gmail.com		Начало
14.05.2018	w5277c@gmail.com		Разве у элементов массива могут быть имена?...
27.03.2019	w5277c@gmail.com		Потоковая запись
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JArray extends JObject {
	public JArray(String l_id) {
		super(l_id);
	}

	@Override
	protected void parse(InputStreamReader l_isr) throws Exception {
		while(true) {
			char ch = skip(l_isr, SPACE);
			if(ch == NEW_LINE) {
				ch = skip(l_isr, NEW_LINE);
			}

			JObject jobj = null;
			switch(ch) {
				case CLOSE_BRACKET:
				case SQ_CLOSE_BRACKET:
					break;
				case OPEN_BRACKET:
					jobj = new JObject();
					((JObject)jobj).parse(l_isr);
					ch = (char)l_isr.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case SQ_OPEN_BRACKET:
					jobj = new JArray((String)null);
					((JArray)jobj).parse(l_isr);
					ch = (char)l_isr.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case QUOT_MARK:
					jobj = new JString((String)null);
					((JString)jobj).parse(l_isr);
					ch = (char)l_isr.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case T:
				case F:
					jobj = new JBoolean((String)null);
					ch = ((JBoolean)jobj).parse(l_isr, ch);
					break;
				default:
					jobj = new JNumber((String)null);
					ch = ((JNumber)jobj).parse(l_isr, ch);
					break;
			}
			if(null != jobj) {
				jitems.add(jobj);
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

	@Override
	protected void parse(InputStream l_is) throws Exception {
		while(true) {
			char ch = skip(l_is, SPACE);
			if(ch == NEW_LINE) {
				ch = skip(l_is, NEW_LINE);
			}

			JObject jobj = null;
			switch(ch) {
				case CLOSE_BRACKET:
				case SQ_CLOSE_BRACKET:
					break;
				case OPEN_BRACKET:
					jobj = new JObject();
					((JObject)jobj).parse(l_is);
					ch = (char)l_is.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case SQ_OPEN_BRACKET:
					jobj = new JArray((String)null);
					((JArray)jobj).parse(l_is);
					ch = (char)l_is.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case QUOT_MARK:
					jobj = new JString((String)null);
					((JString)jobj).parse(l_is);
					ch = (char)l_is.read();
					if(65535 == ch) {
						throw new UnexpectedEndException();
					}
					break;
				case T:
				case F:
					jobj = new JBoolean((String)null);
					ch = ((JBoolean)jobj).parse(l_is, ch);
					break;
				default:
					jobj = new JNumber((String)null);
					ch = ((JNumber)jobj).parse(l_is, ch);
					break;
			}
			if(null != jobj) {
				jitems.add(jobj);
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

	@Override
	public void write(OutputStream l_os) throws IOException {
		super.write_left(l_os);
		l_os.write(SQ_OPEN_BRACKET);
		if(!jitems.isEmpty()) {
			for(JObject jobj : jitems) {
				jobj.write(l_os);
				if(jitems.getLast() != jobj) {
					l_os.write(COMMA);
				}
			}
		}
		l_os.write(SQ_CLOSE_BRACKET);
		super.write_right(l_os);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(SQ_OPEN_BRACKET);
		if(!jitems.isEmpty()) {
			for(JObject jobj : jitems) {
				sb.append(jobj.toString());
				sb.append(COMMA);
			}
			sb.deleteCharAt(sb.length() - 0x01);
		}
		sb.append(SQ_CLOSE_BRACKET);
		return super.toString(sb.toString());
	}
}
