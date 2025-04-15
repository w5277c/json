/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
22.05.2016	w5277c@gmail.com		Начало
31.07.2016	w5277c@gmail.com		Поддержка кодировки
27.03.2019	w5277c@gmail.com		Потоковая запись
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

import java.io.IOException;
import java.io.OutputStream;

public class JBytes extends JObject {
	JBytes(String l_id) {
		super(l_id);
	}

	public JBytes(String l_id, byte[] l_value) {
		super(l_id);
		value = null == l_value ? "null" : Utils.printHexBinary(l_value);
	}

	@Override
	public String toString(String l_value) {
		return (null == id ? l_value : QUOT_MARK + escape(id) + QUOT_MARK + DELIMETER + QUOT_MARK + escape(l_value) + QUOT_MARK);
	}

	@Override
	void write_left(OutputStream l_os) throws IOException {
		if(null != id) {
			StringBuilder sb = new StringBuilder().append(QUOT_MARK).append(escape(id)).append(QUOT_MARK).append(DELIMETER).append(QUOT_MARK);
			l_os.write(sb.toString().getBytes("UTF-8"));
		}
	}

	@Override
	void write_right(OutputStream l_os) throws IOException {
		if(null != id) {
			l_os.write(QUOT_MARK);
		}
	}

	public void set_value(byte[] l_value) {
		value = null == l_value ? "null" : Utils.printHexBinary(l_value);
	}
}
