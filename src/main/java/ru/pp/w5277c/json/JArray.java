/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
15.04.2016	w5277c@gmail.com		Начало
14.05.2018	w5277c@gmail.com		Разве у элементов массива могут быть имена?...
27.03.2019	w5277c@gmail.com		Потоковая запись
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

public class JArray extends JObject {
	public JArray() {
		super();
	}
	public JArray(String id) {
		super(id);
	}

	@Override
	protected void parse(Reader reader) throws Exception {
		while(true) {
			char c = skip(reader, SPACE, TAB, '\r', '\n');

			JObject jobj = null;
			switch(c) {
				case CLOSE_BRACKET:
				case SQ_CLOSE_BRACKET:
					break;
				case OPEN_BRACKET:
					jobj = new JObject();
					((JObject)jobj).parse(reader);
					c = (char)reader.read();
					if(65535 == c) {
						throw new UnexpectedEndException();
					}
					break;
				case SQ_OPEN_BRACKET:
					jobj = new JArray((String)null);
					((JArray)jobj).parse(reader);
					c = (char)reader.read();
					if(65535 == c) {
						throw new UnexpectedEndException();
					}
					break;
				case QUOT_MARK:
					jobj = new JString((String)null);
					((JString)jobj).parse(reader);
					c = (char)reader.read();
					if(65535 == c) {
						throw new UnexpectedEndException();
					}
					break;
				case T:
				case F:
					jobj = new JBoolean((String)null);
					c = ((JBoolean)jobj).parse(reader, c);
					break;
				default:
					jobj = new JNumber((String)null);
					c = ((JNumber)jobj).parse(reader, c);
					break;
			}
			if(null != jobj) {
				jitems.add(jobj);
			}

			if(c == SPACE || c == TAB || c == '\r' || c == '\n') {
				c = skip(reader, SPACE, TAB, '\r', '\n');
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

	@Override
	public void write(OutputStream os) throws IOException {
		super.writePrefix(os);
		os.write(SQ_OPEN_BRACKET);
		if(!jitems.isEmpty()) {
			for(JObject jobj : jitems) {
				jobj.write(os);
				if(jitems.getLast() != jobj) {
					os.write(COMMA);
				}
			}
		}
		os.write(SQ_CLOSE_BRACKET);
		super.writePostfix(os);
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
