/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，对手机存储中的文件进行操作
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
 */
package tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.util.Log;

public class FileUtils {
	private String SDCardRoot;
	private static boolean isCardExist;
	
	public FileUtils() throws NoSdcardException {
		getSDCardRoot();
	}
	
	public String getSDCardRoot() throws NoSdcardException{
		if(isCardExist()){
			SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
		}else{
			throw new NoSdcardException();
		}
		return SDCardRoot;
	}
	
	public static boolean isCardExist(){
		isCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)?true:false;
		return isCardExist;	
	}
	public File createFileInSDCard(String fileName, String dir)
			throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		if(!file.exists()){	
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}
	public File creatSDDir(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}

		return dirFile;
	}
	public boolean filterFileExist(String path, String filter) {
		File file = new File(SDCardRoot + path + File.separator);
		if (file.exists() && file.isDirectory()) {

			String[] fileNames = file.list(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(".png");
				}
			});
			if (fileNames.length > 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * 閸掋倖鏌嘢D閸椻?茬瑐閻ㄥ嫭鏋冩禒鑸垫Ц閸氾箑鐡ㄩ崷?
	 */
	public boolean isFileExist(String fileName, String path) {
		File file = new File(SDCardRoot + path + File.separator + fileName);
		return file.exists();
	}
	public File getFile(String fileName,String path){
		File file = new File(SDCardRoot + path + File.separator + fileName);
		return file;
	}
	public void deleteFile(String fileName, String path) {
		File file = new File(SDCardRoot + path + File.separator + fileName);
		boolean result = file.delete();
	}
	
	public void closeInputStream(InputStream inputStream){
		if(inputStream!=null){
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.e("error", "close failed");
				e.printStackTrace();
			}
		}
	}
	public class NoSdcardException extends Exception{
		
	}
	
	
	/**
	 * 鍒犻櫎鏂囦欢澶逛腑鐨勫唴瀹?,涓嶅垹闄ゆ枃浠跺す鏈韩
	 * @param path
	 */
	public static void deleteDirectoryContent(String path){
		Log.w("test","deleteDirectory.."+path);
		File file=new File(path);
		if(!file.exists()){
			return;
		}
		String fPath=file.getAbsolutePath();
		if(file.isDirectory()){
			String[] files=getDirectoryFiles(path);
			if(files==null){
				deleteFile(path);
				return;
			}
			for(String str:files){
				str=fPath+"/"+str;
			    file=new File(str);
				if(file.isDirectory()){
					deleteDirectory(str);
				}else if(file.isFile()){
					deleteFile(str);
				}
			}
//			deleteFile(path);
		}else if(file.isFile()){
			deleteFile(path);
		}
	}
	
	/**
	 * 鍒犻櫎鏂囦欢澶逛腑鐨勫唴瀹?
	 * @param path
	 */
	public static void deleteDirectory(String path){
		Log.w("test","deleteDirectory.."+path);
		File file=new File(path);
		if(!file.exists()){
			return;
		}
		String fPath=file.getAbsolutePath();
		if(file.isDirectory()){
			String[] files=getDirectoryFiles(path);
			if(files==null){
				deleteFile(path);
				return;
			}
			for(String str:files){
				str=fPath+"/"+str;
			    file=new File(str);
				if(file.isDirectory()){
					deleteDirectory(str);
				}else if(file.isFile()){
					deleteFile(str);
				}
			}
			deleteFile(path);
		}else if(file.isFile()){
			deleteFile(path);
		}
	}
	
	/**
	 * 鍒犻櫎鎸囧畾璺緞鐨勬枃浠?
	 * @param filePath
	 *        鏂囦欢璺緞
	 */
	public static void deleteFile(String filePath){
		Log.w("test","deleteFile:filePath="+filePath);
		if(filePath==null){
			return;
		}
		File file=new File(filePath);
		if(file.exists()){
			file.delete();
		}
	}
	
	
	/**
	 * 鑾峰彇鏂囦欢澶逛笅闈㈢殑鎵?鏈夋枃浠?
	 * @param path
	 * @return
	 */
	public static String[] getDirectoryFiles(String path){
		if(path==null){
			return null;
		}
		File file=new File(path);
		if(!file.exists()){
			return null;
		}
		String[] files=file.list();
		if(files==null || files.length<=0){
			return null;
		}
		return files;
	}
}
