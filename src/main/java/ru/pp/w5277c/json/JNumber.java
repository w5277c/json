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
import java.math.BigDecimal;
import java.math.RoundingMode;

public class JNumber extends JObject {
	JNumber(String l_id) {
		super(l_id);
	}

	public JNumber(String l_id, Long l_value) {
		super(l_id);
		value = null == l_value ? "null" : Long.toString(l_value);
	}
	public JNumber(String l_id, Integer l_value) {
		super(l_id);
		value = null == l_value ? "null" : Integer.toString(l_value);
	}
	public JNumber(String l_id, Byte l_value) {
		super(l_id);
		value = null == l_value ? "null" : Byte.toString(l_value);
	}
	public JNumber(String l_id, Double l_value) {
		this(l_id, l_value, null);
	}
	public JNumber(String l_id, Double l_value, Integer l_scale) {
		super(l_id);
		if(null == l_value) {
			value = "null";
		}
		else {
			if(null == l_scale) {
				value = Double.toString(l_value);
			}
			else {
				BigDecimal bd = (null == l_scale ? new BigDecimal(l_value) : new BigDecimal(l_value).setScale(l_scale, RoundingMode.HALF_UP));
				value = bd.toString();
			}
		}
	}

	public char parse(InputStreamReader l_isr, char l_ch) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l_ch);
		char ch = read(l_isr, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET);
		try {
			if("null".equals(sb.toString())) {
				value = "null";
			}
			else {
				Double.parseDouble(sb.toString());
				value = sb.toString();
			}
		}
		catch(Exception ex) {
			throw new ParseException("Incorrect value:" + sb.toString());
		}
		return ch;
	}
	public char parse(InputStream l_is, char l_ch) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(l_ch);
		char ch = read(l_is, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET);
		try {
			if("null".equals(sb.toString())) {
				value = "null";
			}
			else {
				Double.parseDouble(sb.toString());
				value = sb.toString();
			}
		}
		catch(Exception ex) {
			throw new ParseException("Incorrect value:" + sb.toString());
		}
		return ch;
	}

	public int get_int() {
		return "null".equals(value) ? null : Integer.parseInt(value);
	}

	public long get_long() {
		return "null".equals(value) ? null : Long.parseLong(value);
	}

	public void set_Value(Long l_value) {
		value = null == l_value ? null : Long.toString(l_value);
	}

	public void set_value(Double l_value) {
		value = null == l_value ? null : Double.toString(l_value);
	}
}
