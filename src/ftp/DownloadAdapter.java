package ftp;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;

/**
 * 下载适配器
 * 
 * @author zhangping
 * 
 */
public class DownloadAdapter implements Runnable {
	private String rem_file = "";
	private String local_file = "";
	private FTPClient ftp_down = null;

	public DownloadAdapter(String rem_file, String local_file) {
		try {
			this.rem_file = rem_file;
			this.local_file = local_file;
			ftp_down = Util.getFTPClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		OutputStream out = null;
		try {
			out = new FileOutputStream(local_file);
			ftp_down.retrieveFile(rem_file, out);
			int reply = ftp_down.getReplyCode();
			if (reply == 226) {
				Util.p(rem_file + " is download: " + local_file);
			}
		} catch (Exception e) {
			Util.p("file:::" + rem_file);
			e.printStackTrace();
			Util.writeLog("exception", "[" + rem_file + "]" + e.toString());
		} finally {
			try {
				if (ftp_down.isConnected()) {
					try {
						ftp_down.logout();
						ftp_down.disconnect();
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
