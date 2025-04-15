/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
15.04.2016	w5277c@gmail.com		Начало
13.05.2016	w5277c@gmail.com		Скопировано с основной библиотеки
21.05.2016	w5277c@gmail.com		Переделано для работы с потоком
31.07.2016	w5277c@gmail.com		Поддержка кодировки
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JBoolean extends JObject {
	JBoolean(String l_id) {
		super(l_id);
	}

	public JBoolean(String l_id, boolean l_is_true) {
		super(l_id);
		value = l_is_true ? TRUE : FALSE;
	}

	public char parse(InputStreamReader l_isr, char l_ch) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l_ch);
		char ch = read(l_isr, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET);
		if(sb.toString().toLowerCase().equals(TRUE) || sb.toString().toLowerCase().equals(FALSE)) {
			value = sb.toString().toLowerCase();
		}
		else {
			throw new ParseException("Incorrect value:" + sb.toString());
		}
		return ch;
	}

	public char parse(InputStream l_is, char l_ch) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l_ch);
		char ch = read(l_is, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET);
		if(sb.toString().toLowerCase().equals(TRUE) || sb.toString().toLowerCase().equals(FALSE)) {
			value = sb.toString().toLowerCase();
		}
		else {
			throw new ParseException("Incorrect value:" + sb.toString());
		}
		return ch;
	}
}
