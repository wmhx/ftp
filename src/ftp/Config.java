package ftp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 配置
 * 
 * @author zhangping
 * 
 */
public class Config {

	public String PROPERTIES_FILE = "";

	// / 需要下载的文件后缀
	public static String[] _ext = {};

	// / 不需要下载的目录
	public static String[] _not = {};

	// / 本地存储目录
	public static String local_path = "";
	// / 远程下载目录
	public static String path = "";

	// encode ,default GBK
	public static String encoding = "";
	// /username
	public static String username = "";
	// /password
	public static String password = "";
	// / HOST IP
	public static String HOST = "";
	// / err.log
	public static String ERR_LOG = "";
	// /ftp timeout
	public static int timeout = 6;
	// /ftp port
	public static int PORT = 21;
	// /下载文件大小限制
	public static long MAX_SIZE = 1;// (1MB)
	// /被动模式
	public static boolean isPASV = false;
	// /是否覆盖
	public static boolean cover = false;
	// /线程池大小
	public static int threads = 1;

	/**
	 * 初始化参数
	 * 
	 * @param file
	 */
	public static void init(String file) {
		Config dao = new Config(file);
		local_path = dao.readValue("local_path", "").trim();
		path = dao.readValue("path", "").trim();
		username = dao.readValue("username", "").trim();
		password = dao.readValue("password", "").trim();
		HOST = dao.readValue("HOST", "").trim();
		encoding = dao.readValue("encoding", "GBK").trim();
		isPASV = dao.readValue("isPASV", "true").trim().equals("true");
		cover = dao.readValue("cover", "false").trim().equals("true");
		ERR_LOG = dao.readValue("ERR_LOG", "./err.log").trim();
		PORT = Integer.parseInt(dao.readValue("PORT", "21").trim()); // ftp.port
		threads = Integer.parseInt(dao.readValue("threads", "1").trim()); // /线程池大小
		timeout = Integer.parseInt(dao.readValue("timeout", "1").trim()); // /超时时间
		MAX_SIZE = Long.valueOf(dao.readValue("MAX_SIZE", "1").trim());
		String _ext_string = dao.readValue("_ext", "*").trim().replaceAll(" ", "").replaceAll(",,", ",");
		String _not_string = dao.readValue("_not", "").trim().replaceAll(" ", "").replaceAll(",,", ",");

		if (MAX_SIZE < 1) {
			MAX_SIZE = 1;
			System.out.println("config[MAX_SIZE] must biger than 0");
		}
		MAX_SIZE = MAX_SIZE * 1024 * 1024; // 转换成MB

		if (threads < 1) {
			threads = 1;
			System.out.println("config[threads] must biger than 0");
		}

		if (_ext_string.equals("")) {
			_ext_string = "*";
		}

		_ext = _ext_string.split(",");
		_not = _not_string.split(",");

		ERR_LOG = ERR_LOG.substring(0, ERR_LOG.lastIndexOf(".")) + "-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + "." + ERR_LOG.substring(ERR_LOG.lastIndexOf(".") + 1);
		System.out.println(ERR_LOG);
	}

	// 读取properties的全部信息
	private void readProperties() {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(PROPERTIES_FILE));
			props.load(in);
			Enumeration en = props.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String Property = props.getProperty(key);
				System.out.println(key + Property);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ////////////根据key读取value
	public String readValue(String key, String defaultvalue) {
		String t = readValue(key);
		return t == null ? defaultvalue : t;
	}

	// ////////////根据key读取value
	private String readValue(String key) {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(PROPERTIES_FILE));
			props.load(in);
			String value = props.getProperty(key);
			System.out.println(key + "=" + value);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Config(String PROPERTIES_FILE) {
		this.PROPERTIES_FILE = PROPERTIES_FILE;
	}

	// public static void main(String[] args) {
	// Config dao = new Config("");
	//
	// String aaa = dao.readValue("HOST");
	//
	// // System.out.println("====>aaa=" + aaa);
	//
	// }

}
