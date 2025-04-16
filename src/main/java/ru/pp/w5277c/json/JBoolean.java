/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
15.04.2016	w5277c@gmail.com		Начало
13.05.2016	w5277c@gmail.com		Скопировано с основной библиотеки
21.05.2016	w5277c@gmail.com		Переделано для работы с потоком
31.07.2016	w5277c@gmail.com		Поддержка кодировки
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

import java.io.Reader;

public class JBoolean extends JObject {
	JBoolean(String id) {
		super(id);
	}

	public JBoolean(String id, boolean isTrue) {
		super(id);
		value = isTrue ? TRUE : FALSE;
	}

	public char parse(Reader reader, char c) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(c);
		char _c = read(reader, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET, '\r', '\n', '\t'); //todo use isAlpha
		if(sb.toString().toLowerCase().equals(TRUE) || sb.toString().toLowerCase().equals(FALSE)) {
			value = sb.toString().toLowerCase();
		}
		else {
			throw new ParseException("Incorrect value:" + sb.toString());
		}
		return _c;
	}
}
