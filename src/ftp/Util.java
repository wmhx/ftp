package ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

/**
 * 杂
 * 
 * @author zhangping
 * 
 */
public class Util {

	// / log writer
	private static LogWriter logWriter = null;

	public static void initLog() throws IOException {
		logWriter = new LogWriter(Config.ERR_LOG);
	}

	public static void closeLog() {
		logWriter.closeLog();
	}

	public static void writeLog(String type, String msg) {
		logWriter.write(type, msg);
	}

	/**
	 * logogram for p();
	 * 
	 * @param s
	 */
	public static void p(String s) {
		System.out.println(s);
	}

	/**
	 * @return
	 * @throws Exception
	 */

	public static FTPClient getFTPClient() throws Exception {
		FTPClient ftpInstance = new FTPClient();
		ftpInstance.connect(Config.HOST, Config.PORT);
		ftpInstance.login(Config.username, Config.password);// 登录
		ftpInstance.setControlKeepAliveTimeout(Config.timeout * 1000);
		ftpInstance.setControlEncoding(Config.encoding);
		if (Config.isPASV) {
			ftpInstance.enterLocalPassiveMode(); // 被动模式
		} else {
			ftpInstance.enterLocalActiveMode(); // 主动模式
		}
		return ftpInstance;
	}
}
