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
	JNumber(String id) {
		super(id);
	}

	public JNumber(String id, Long value) {
		super(id);
		this.value = null == value ? "null" : Long.toString(value);
	}
	public JNumber(String id, Integer value) {
		super(id);
		this.value = null == value ? "null" : Integer.toString(value);
	}
	public JNumber(String id, Byte value) {
		super(id);
		this.value = null == value ? "null" : Byte.toString(value);
	}
	public JNumber(String id, Double value) {
		this(id, value, null);
	}
	public JNumber(String id, Double value, Integer scale) {
		super(id);
		if(null == value) {
			this.value = "null";
		}
		else {
			if(null == scale) {
				this.value = Double.toString(value);
			}
			else {
				BigDecimal bd = (null == scale ? new BigDecimal(value) : new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP));
				this.value = bd.toString();
			}
		}
	}

	public char parse(InputStreamReader isr, char c) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(c);
		char _c = read(isr, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET);
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
		return _c;
	}
	public char parse(InputStream is, char c) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(c);
		char ch = read(is, sb, COMMA, SPACE, CLOSE_BRACKET, SQ_CLOSE_BRACKET);
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

	public int getInt() {
		return "null".equals(value) ? null : Integer.parseInt(value);
	}

	public long getLong() {
		return "null".equals(value) ? null : Long.parseLong(value);
	}

	public void setValue(Long value) {
		this.value = null == value ? null : Long.toString(value);
	}

	public void setValue(Double value) {
		this.value = null == value ? null : Double.toString(value);
	}
}
