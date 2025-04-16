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
	public JBytes(String id, byte[] value) {
		super(id);
		this.value = null == value ? "null" : Utils.printHexBinary(value);
	}

	@Override
	public String toString(String value) {
		return (null == id ? value : QUOT_MARK + escape(id) + QUOT_MARK + DELIMETER + QUOT_MARK + escape(value) + QUOT_MARK);
	}

	@Override
	void writePrefix(OutputStream os) throws IOException {
		if(null != id) {
			os.write((QUOT_MARK + escape(id) + QUOT_MARK + DELIMETER + QUOT_MARK).getBytes("UTF-8"));
		}
	}

	@Override
	void writePostfix(OutputStream os) throws IOException {
		if(null != id) {
			os.write(QUOT_MARK);
		}
	}

	public void set_value(byte[] value) {
		this.value = null == value ? "null" : Utils.printHexBinary(value);
	}
}
