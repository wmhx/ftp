package ftp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

/**
 * 多线程按目录批量下载
 * main
 * @author zhangping
 * 
 */
public class ZPFTPApp {

	private FTPClient login_ftp;
	// /线程池
	private ExecutorService pool = null;

	/**
	 * 连接下载fpt服务器
	 * 
	 * @param server
	 * @param port
	 * @param user
	 * @param password
	 * @throws IOException
	 */
	public boolean connectServer() throws Exception {
		// server：FTP服务器的IP地址；user:登录FTP服务器的用户名
		// password：登录FTP服务器的用户名的口令；path：FTP服务器上的路径
		login_ftp = Util.getFTPClient();
		if (login_ftp == null) {
			p("ERROR");
			writeLog("error", "ftp err , make sure your config");
			return false;
		}
		for (String s : login_ftp.getReplyStrings()) {
			p(s);
		}
		return true;
	}

	/**
	 * 关闭ftp连接
	 */
	public void closeServer() {
		try {
			login_ftp.logout();
			if (login_ftp != null) {
				login_ftp.disconnect();
				login_ftp = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭线程池
	 */
	private void shutdownPool() {
		pool.shutdown();
	}

	/**
	 * 初始化线程池 固定大小
	 */
	private void initThreadPool() {
		pool = Executors.newFixedThreadPool(Config.threads);
	}

	/**
	 * 下载指定目录
	 * 
	 * @param path
	 * @throws IOException
	 */
	public void download(String path) throws Exception {
		if(!connectServer()){
			writeLog("error", "ftp err , make sure your config");
			return;
		}

		path = path.replace("\\", "/").replace("//", "/");
		p(">>>目录:" + path);

		if (notDownloadDir(path)) {
			p("path[" + path + "] is ignore");
			return;
		}

		FTPFile[] fileList = login_ftp.listFiles(path, new FTPFileFilter() {
			public boolean accept(FTPFile paramFTPFile) {
				try {
					return paramFTPFile.getType() != FTPFile.DIRECTORY_TYPE;
				} catch (RuntimeException e) {
					return false;
				}
			}
		}); // file
		FTPFile[] fileDirectory = login_ftp.listDirectories(path); // directory

		// / download file
		for (int i = 0; i < fileList.length; i++) {
			ftp_down(path, fileList[i]);
		}

		for (int i = 0; i < fileDirectory.length; i++) {
			download(path + File.separator + fileDirectory[i].getName());
		}

	}

	private void writeLog(String type, String msg) {
		Util.writeLog(type, msg);
	}

	/**
	 * 建立本地文件所属目录
	 * 
	 * @param local_file
	 */
	private void createPathifNotExists(String local_file) {
		String fs = new File(local_file).getParent();
		File f = new File(fs);
		if (!f.exists()) {
			p("创建目录:" + f.getAbsolutePath());
			f.mkdirs();
		}
	}

	/**
	 * 不需要下载的目录
	 * 
	 * @param path
	 * @return
	 */
	private boolean notDownloadDir(String path) {
		if (Config._not.length < 1 || Config._not[0].equals("")) {
			return false;
		}
		for (int i = 0; i < Config._not.length; i++) {
			if (path.startsWith(Config._not[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是可以下载的文件
	 * 
	 * @param path
	 * @return
	 */
	private static boolean isOkFile(String file) {
		if (Config._ext[0].trim().equals("*")) {
			return true;
		}
		for (int i = 0; i < Config._ext.length; i++) {
			if (file.endsWith(Config._ext[i])) {
				return true;
			}
		}
		return false;
	}

	private void ftp_down(String path, FTPFile file) {
		try {
			// full path on server
			String rem_file = (path + File.separator + file.getName()).replace('\\', '/');
			String local_file = (Config.local_path + path + File.separator + file.getName()).replace("\\", "/");

			if (!isOkFile(rem_file)) {
				// p(rem_file + " is not allowed to be download");
				return;
			}

			if (file.getSize() > Config.MAX_SIZE) { // 超过大小
				String s = rem_file + "'s size(" + (Double.valueOf(file.getSize() / 1024 / 1024)) + "MB) is more than " + (Config.MAX_SIZE / 1024 / 1024) + "(MB)";
				writeLog("MAX_SIZE", s);
				p(s);
				return;
			}

			if (localFileExists(new File(local_file))) {
				if (Config.cover) {
					deletelocalFile(new File(local_file));
				} else {
					writeLog("exists", local_file + "");
					return;
				}
			}
			createPathifNotExists(local_file);

			newThreadDownload(rem_file, local_file);

		} catch (Exception e) {
		}

	}

	private void newThreadDownload(String rem_file, String local_file) {
		pool.execute(new DownloadAdapter(rem_file, local_file));
	}

	/**
	 * 本地文件是否存在
	 * 
	 * @param localFile
	 * @return
	 */
	private boolean localFileExists(File localFile) {
		return localFile.exists();
	}

	private void deletelocalFile(File localFile) {
		localFile.deleteOnExit();
	}

	private void closeLog() {
		Util.closeLog();
	}

	private void initLog() throws IOException {
		Util.initLog();
	}

	public static void p(String s) {
		Util.p(s);
	}

	private void initConfig(String file) {
		Config.init(file);
	}

	private static void help() {
		int i=1;
		p("=============多线程按目录批量下载=========================");
		p("配置参数[默认值]:");
		p(i+++".\tHOST\t\t(必填)\tFTP主机IP地址");
		p(i+++".\tusername\t(必填)\tFTP用户名");
		p(i+++".\tpassword\t(必填)\tFTP密码");
		p(i+++".\tlocal_path\t(必填)\t本地存储目录");
		p(i+++".\tpath\t\t(必填)\t远程下载目录");
		p(i+++".\t_ext\t\t需要下载的文件后缀,用逗号隔开,全部下载配置为*[*]");
		p(i+++".\t_not\t\t过滤的目录,用逗号隔开[]");
		p(i+++".\tcover\t\t本地是否覆盖,否则跳过[false]");
		p(i+++".\tPORT\t\tFTP服务器端口[21]");
		p(i+++".\tisPASV\t\tFTP被动模式[true]");
		p(i+++".\tMAX_SIZE\tFTP下载最大文件大小(单位MB)[1]");
		p(i+++".\tencoding\tFTP传输编码[GBK]");
		p(i+++".\tthreads\t\t多线程下载数[1]");
		p(i+++".\ttimeout\t\t超时时间(秒)[1]");
		p(i+++".\tERR_LOG\t\t出错文件[err.log]");
		p("===========================================");
		p("\t\t\tby zp-wmhx@163.com");
		p("");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			p("第一个参数是配置文件");
			System.exit(0);
		}
		if (args[0].toLowerCase().indexOf("-help") > -1) {
			help();
			System.exit(0);
		} else if (!new File(args[0]).exists()) {
			p("文件不存在,请确认路径[" + args[0] + "]");
			System.exit(0);
		}

		ZPFTPApp dao = new ZPFTPApp();
		dao.initConfig(args[0]);
		try {
			dao.initLog();
			dao.initThreadPool();
			dao.download(Config.path);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dao.closeServer();
			dao.shutdownPool();
			dao.closeLog();
		}
	}


}
