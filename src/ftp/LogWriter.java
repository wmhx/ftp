package ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
/**
 * 日志记录器
 * @author zhangping
 *
 */

public class LogWriter {
	File file = null;
	FileOutputStream fop = null;

	public LogWriter(String ERR_LOG) throws IOException {
		initLog(ERR_LOG);
		fop = new FileOutputStream(file, true);
	}

	public synchronized void write(String type,String msg) {
		try {
			byte[] contentInBytes = formst(type,msg).getBytes();
			fop.write(contentInBytes);
			fop.flush();
		} catch (IOException e) {
			System.out.println("log writer is error ");
			e.printStackTrace();
		}
	}

	private String formst(String type,String msg) {
		return ("[" + new Date() + "] ["+type+"]"  + msg + "\n");
	}

	public void initLog(String ERR_LOG) throws IOException {
		file = new File(ERR_LOG);
		if (!file.exists()) {
			file.createNewFile();
		}
	}

	public void closeLog() {
		try {
			fop.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
