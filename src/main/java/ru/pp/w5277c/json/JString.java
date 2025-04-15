/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
15.04.2016	w5277c@gmail.com		Начало
13.05.2016	w5277c@gmail.com		Скопировано с основной библиотеки
21.05.2016	w5277c@gmail.com		Переделано для работы с потоком
31.07.2016	w5277c@gmail.com		Поддержка кодировки
14.05.2018	w5277c@gmail.com		Пропущены кавычки для режима без имени
27.03.2019	w5277c@gmail.com		Потоковая запись
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JString extends JObject {
	JString(String id) {
		super(id);
	}
	public JString(String id, String value) {
		super(id);
		this.value = value;
	}

	@Override
	public String toString(String value) {
		return (null == id ? QUOT_MARK + value + QUOT_MARK : QUOT_MARK + escape(id) + QUOT_MARK + DELIMETER + QUOT_MARK + escape(value) + QUOT_MARK);
	}

	@Override
	void writePrefix(OutputStream os) throws IOException {
		if(null == id) {
			os.write(QUOT_MARK);
		}
		else {
			StringBuilder sb = new StringBuilder().append(QUOT_MARK).append(escape(id)).append(QUOT_MARK).append(DELIMETER).append(QUOT_MARK);
			os.write(sb.toString().getBytes("UTF-8"));
		}
	}
	@Override
	void writePostfix(OutputStream os) throws IOException {
		os.write(QUOT_MARK);
	}

	@Override
	public void parse(InputStreamReader isr) throws Exception {
		StringBuilder sb = new StringBuilder();
		readStr(isr, sb);
		value = sb.toString();
	}
	@Override
	public void parse(InputStream is) throws Exception {
		StringBuilder sb = new StringBuilder();
		while(true) {
			readStr(is, sb);
			if(sb.length()<0x02) {
				break;
			}
			if(!sb.substring(sb.length()-0x01, sb.length()).equals("\\")) {
				break;
			}
		}
		value = sb.toString();
	}
}
